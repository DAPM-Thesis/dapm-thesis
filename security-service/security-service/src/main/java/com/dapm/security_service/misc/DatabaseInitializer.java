package com.dapm.security_service.misc;

import com.dapm.security_service.models.*;
import com.dapm.security_service.repositories.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private IOrganizationRepository organizationRepository;

    @Autowired
    private IPermissionRepository permissionRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IUserRepository userRepository;

    // BCrypt encoder to hash passwords
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        // Create or fetch the default organization.
        String orgName = "OrgA";
        Organization organization = organizationRepository.findAll()
                .stream()
                .filter(org -> org.getName().equals(orgName))
                .findFirst()
                .orElse(null);
        if (organization == null) {
            organization = Organization.builder()
                    .id(UUID.randomUUID())
                    .name(orgName)
                    // In a real application, use a system or admin user ID.
                    .createdBy(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            organization = organizationRepository.save(organization);
        }

        // Define permissions needed in our system.
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionMap.put("USER_MANAGEMENT", createPermissionIfNotExist("USER_MANAGEMENT", "Manage users", organization));
        permissionMap.put("ROLE_MANAGEMENT", createPermissionIfNotExist("ROLE_MANAGEMENT", "Manage roles", organization));
        permissionMap.put("APPROVE_ACCESS", createPermissionIfNotExist("APPROVE_ACCESS", "Approve access requests", organization));
        permissionMap.put("CONFIGURE_LIMITS", createPermissionIfNotExist("CONFIGURE_LIMITS", "Configure resource limits", organization));
        permissionMap.put("EXECUTE_PIPELINE", createPermissionIfNotExist("EXECUTE_PIPELINE", "Execute pipeline", organization));
        permissionMap.put("READ_PIPELINE", createPermissionIfNotExist("READ_PIPELINE", "Read pipeline details", organization));
        permissionMap.put("REQUEST_ACCESS", createPermissionIfNotExist("REQUEST_ACCESS", "Request access", organization));
        permissionMap.put("SET_LIMITS", createPermissionIfNotExist("SET_LIMITS", "Set resource usage limits", organization));
        permissionMap.put("ASSIGN_TEMP_ACCESS", createPermissionIfNotExist("ASSIGN_TEMP_ACCESS", "Assign temporary access", organization));

        // Create roles if they don't exist.
        Role adminRole = createRoleIfNotExist("ADMIN", organization, new HashSet<>(Arrays.asList(
                permissionMap.get("USER_MANAGEMENT"),
                permissionMap.get("ROLE_MANAGEMENT"),
                permissionMap.get("APPROVE_ACCESS"),
                permissionMap.get("CONFIGURE_LIMITS"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("REQUEST_ACCESS"),
                permissionMap.get("SET_LIMITS"),
                permissionMap.get("ASSIGN_TEMP_ACCESS")
        )));

        Role depHeadRole = createRoleIfNotExist("DEPARTMENT_HEAD", organization, new HashSet<>(Arrays.asList(
                permissionMap.get("APPROVE_ACCESS"),
                permissionMap.get("SET_LIMITS"),
                permissionMap.get("ASSIGN_TEMP_ACCESS"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("REQUEST_ACCESS")
        )));

        Role researcherRole = createRoleIfNotExist("RESEARCHER", organization, new HashSet<>(Arrays.asList(
                permissionMap.get("REQUEST_ACCESS"),
                permissionMap.get("EXECUTE_PIPELINE")
        )));

        Role researcherLimitedRole = createRoleIfNotExist("RESEARCHER_LIMITED", organization, new HashSet<>(Arrays.asList(
                permissionMap.get("REQUEST_ACCESS"),
                permissionMap.get("READ_PIPELINE")
        )));

        // Create users with password "dapm" (hashed) if they don't exist.
        createUserIfNotExist("anna", "anna@example.com", "dapm", adminRole, organization);
        createUserIfNotExist("anthoni", "anthoni@example.com", "dapm", depHeadRole, organization);
        createUserIfNotExist("alice", "alice@example.com", "dapm", researcherRole, organization);
        createUserIfNotExist("ashley", "ashley@example.com", "dapm", researcherLimitedRole, organization);
    }

    private Permission createPermissionIfNotExist(String name, String description, Organization organization) {
        Permission permission = permissionRepository.findByName(name);
        if (permission == null) {
            permission = Permission.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .description(description)
                    // Use a placeholder for createdBy; adjust as needed.
                    .createdBy(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    private Role createRoleIfNotExist(String name, Organization organization, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = Role.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .organization(organization)
                    .permissions(permissions)
                    .createdBy(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            role = roleRepository.save(role);
        }
        return role;
    }

    private void createUserIfNotExist(String username, String email, String rawPassword, Role role, Organization organization) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            // Hash the password using BCrypt.
            String passwordHash = passwordEncoder.encode(rawPassword);
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .organization(organization)
                    .roles(new HashSet<>(Collections.singletonList(role)))
                    .createdBy(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            userRepository.save(user);
        }
    }
}
