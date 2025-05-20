package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.models.dtos.UserDto;
import com.dapm.security_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserDto::new).toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable UUID id) {
        return userRepository.findById(id).map(UserDto::new).orElse(null);
    }

    @PostMapping
    public UserDto createUser(@RequestBody CreateUserDto user) {
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

        return new UserDto(userRepository.save(newUser));
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable UUID id, @RequestBody User user) {
        user.setId(id);
        return new UserDto(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
    }
}
