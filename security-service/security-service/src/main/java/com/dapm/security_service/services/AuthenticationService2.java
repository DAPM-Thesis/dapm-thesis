package com.dapm.security_service.services;

import com.dapm.security_service.config.JwtService;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService2 {

    @Autowired private UserDetailsRepository repository;
    @Autowired private OrganizationRepository organizationRepository;

    @Autowired private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(CreateUserDto user) {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail(user.getEmail());
        newUser.setUsername(user.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        Organization organization = organizationRepository.findByName(user.getOrganization())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        newUser.setOrganization(organization);



        Set<Role> roles = user.getRoles().stream()
                .map(roleName -> {
                    Role role = roleRepository.findByName(roleName);
                    if (role == null) {
                        role = new Role();
                        role.setName(roleName);
                        role = roleRepository.save(role);
                    }
                    return role;
                })
                .collect(Collectors.toSet());
        newUser.setRoles(roles);

        repository.save(newUser);
        String jwtToken = jwtService.generateToken(newUser);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
