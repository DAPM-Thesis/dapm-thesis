package com.dapm.security_service.services;

import com.dapm.security_service.config.JwtService;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.models.dtos.UserDto;
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


    @Autowired
    private UserDetailsRepository repository;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(CreateUserDto user){
        User newUser=new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail(user.getEmail());
        newUser.setUsername(user.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        Organization organization = organizationRepository.findByName(user.getOrganization());
        if (organization == null) {
            throw new IllegalArgumentException("Organization not found");
        }
        newUser.setOrganization(organization);

        Faculty faculty = facultyRepository.findByName(user.getFaculty());
        if (faculty == null) {
            throw new IllegalArgumentException("Faculty not found");
        }
        newUser.setFaculty(faculty);

        Department department = departmentRepository.findByName(user.getDepartment());
        if (department == null) {
            throw new IllegalArgumentException("Department not found");
        }
        newUser.setDepartment(department);

        Set<Role> roles = user.getRoles().stream()
                .map(roleName -> {
                    Role role = roleRepository.findByName(roleName);
                    if (role == null) { // If not found, create a new one
                        role = new Role();
                        role.setName(roleName);
                        role = roleRepository.save(role); // Save new role
                    }
                    return role;
                })
                .collect(Collectors.toSet());
        newUser.setRoles(roles);


        repository.save(newUser);
        var jwtToke= jwtService.generateToken(newUser);

        AuthResponse res=new AuthResponse(jwtToke);
        return res;
    }

    public AuthResponse authenticate(AuthRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToke= jwtService.generateToken(user);
        AuthResponse res=new AuthResponse(jwtToke);
        return res;
    }
}
