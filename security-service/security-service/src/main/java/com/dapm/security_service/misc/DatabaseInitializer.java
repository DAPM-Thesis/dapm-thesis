package com.dapm.security_service.misc;

import com.dapm.security_service.models.*;
import com.dapm.security_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PolicyRepository policyRepository;

    // BCrypt encoder to hash passwords.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        // 1. Create or fetch the default organization.
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
                    .build();
            organization = organizationRepository.save(organization);
        }

        // 2. Create Faculty "Computer Science" for the organization.
        Faculty faculty = facultyRepository.findByName("Computer Science");
        if (faculty == null) {
            faculty = Faculty.builder()
                    .id(UUID.randomUUID())
                    .name("Computer Science")
                    .organization(organization)
                    .build();
            faculty = facultyRepository.save(faculty);
        }

        // 3. Create Department "Software Engineering" under "Computer Science".
        Department department = departmentRepository.findByName("Software Engineering");
        if (department == null) {
            department = Department.builder()
                    .id(UUID.randomUUID())
                    .name("Software Engineering")
                    .faculty(faculty)
                    .build();
            department = departmentRepository.save(department);
        }

        // 4. Define permissions.
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionMap.put("APPROVE_ACCESS", createPermissionIfNotExist("APPROVE_ACCESS", "Approve access requests"));
        permissionMap.put("SET_LIMITS", createPermissionIfNotExist("SET_LIMITS", "Set resource usage limits"));
        permissionMap.put("READ_PIPELINE", createPermissionIfNotExist("READ_PIPELINE", "Read pipeline details"));
        permissionMap.put("EXECUTE_PIPELINE", createPermissionIfNotExist("EXECUTE_PIPELINE", "Execute pipeline"));
        permissionMap.put("REQUEST_ACCESS", createPermissionIfNotExist("REQUEST_ACCESS", "Request access"));
        permissionMap.put("CONFIGURE_CROSS_ORG_TRUST", createPermissionIfNotExist("CONFIGURE_CROSS_ORG_TRUST", "Configure cross organization trust policies"));
        permissionMap.put("EXCHANGE_PUBLIC_KEYS", createPermissionIfNotExist("EXCHANGE_PUBLIC_KEYS", "Exchange public keys with external organizations"));
        permissionMap.put("ROLE_MANAGEMENT", createPermissionIfNotExist("ROLE_MANAGEMENT", "Manage roles"));
        permissionMap.put("CREATE_PIPELINE", createPermissionIfNotExist("CREATE_PIPELINE", "Create new pipeline"));
        permissionMap.put("UPLOAD_RESOURCE", createPermissionIfNotExist("UPLOAD_RESOURCE", "Upload resource"));
        permissionMap.put("DELETE_RESOURCE", createPermissionIfNotExist("DELETE_RESOURCE", "Delete resource"));
        permissionMap.put("READ_RESOURCE", createPermissionIfNotExist("READ_RESOURCE", "Read resource"));
        permissionMap.put("EDIT_RESOURCE", createPermissionIfNotExist("EDIT_RESOURCE", "Edit resource"));
        permissionMap.put("DOWNLOAD_RESOURCE", createPermissionIfNotExist("DOWNLOAD_RESOURCE", "Download resource"));
        permissionMap.put("ACCESS_RESOURCE", createPermissionIfNotExist("ACCESS_RESOURCE", "Access resource"));
        permissionMap.put("MODIFY_RESOURCE", createPermissionIfNotExist("MODIFY_RESOURCE", "Modify resource"));

        // 5. Create roles.
        // ADMIN: Inherits everything from Department_Head and Researcher, plus extra admin privileges.
        Set<Permission> adminPerms = new HashSet<>(Arrays.asList(
                permissionMap.get("APPROVE_ACCESS"),
                permissionMap.get("SET_LIMITS"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("REQUEST_ACCESS"),
                permissionMap.get("CREATE_PIPELINE"),
                permissionMap.get("UPLOAD_RESOURCE"),
                permissionMap.get("DELETE_RESOURCE"),
                permissionMap.get("READ_RESOURCE"),
                permissionMap.get("EDIT_RESOURCE"),
                permissionMap.get("DOWNLOAD_RESOURCE"),
                permissionMap.get("CONFIGURE_CROSS_ORG_TRUST"),
                permissionMap.get("EXCHANGE_PUBLIC_KEYS"),
                permissionMap.get("ROLE_MANAGEMENT")
        ));
        Role adminRole = createRoleIfNotExist("ADMIN", organization, adminPerms);

        // DEPARTMENT_HEAD role.
        Set<Permission> depHeadPerms = new HashSet<>(Arrays.asList(
                permissionMap.get("APPROVE_ACCESS"),
                permissionMap.get("SET_LIMITS"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("REQUEST_ACCESS")
        ));
        Role depHeadRole = createRoleIfNotExist("DEPARTMENT_HEAD", organization, depHeadPerms);

        // RESEARCHER role.
        Set<Permission> researcherPerms = new HashSet<>(Arrays.asList(
                permissionMap.get("REQUEST_ACCESS"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("CREATE_PIPELINE"),
                permissionMap.get("UPLOAD_RESOURCE"),
                permissionMap.get("DELETE_RESOURCE"),
                permissionMap.get("READ_RESOURCE"),
                permissionMap.get("EDIT_RESOURCE"),
                permissionMap.get("DOWNLOAD_RESOURCE")
        ));
        Role researcherRole = createRoleIfNotExist("RESEARCHER", organization, researcherPerms);

        // PIPELINE_ROLE: This role will be assigned to pipelines and contains resource access permissions.
        Set<Permission> pipelinePerms = new HashSet<>(Arrays.asList(
                permissionMap.get("READ_RESOURCE"),
                permissionMap.get("ACCESS_RESOURCE"),
                permissionMap.get("MODIFY_RESOURCE"),
                permissionMap.get("DELETE_RESOURCE")
        ));
        Role pipelineRole = createRoleIfNotExist("PIPELINE_ROLE", organization, pipelinePerms);

        // 6. Create users with password "dapm" (hashed) if they don't exist.
        createUserIfNotExist("anna", "anna@example.com", "dapm", adminRole, organization, faculty, department);
        createUserIfNotExist("anthoni", "anthoni@example.com", "dapm", depHeadRole, organization, faculty, department);
        createUserIfNotExist("alice", "alice@example.com", "dapm", researcherRole, organization, faculty, department);
        createUserIfNotExist("ashley", "ashley@example.com", "dapm", researcherRole, organization, faculty, department);

        // 7. Create a policy for EXECUTE_PIPELINE permission: allow only users from "Software Engineering" (department)
        // and "Computer Science" (faculty).
        createPolicyIfNotExist(permissionMap.get("EXECUTE_PIPELINE"), department, faculty, "ALLOW");

        System.out.println("Database initialization complete.");
    }

    private Permission createPermissionIfNotExist(String name, String description) {
        Permission permission = permissionRepository.findByName(name);
        if (permission == null) {
            permission = Permission.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .description(description)
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
                    .build();
            role = roleRepository.save(role);
        }
        return role;
    }

    private void createUserIfNotExist(String username, String email, String rawPassword, Role role, Organization organization, Faculty faculty, Department department) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            String passwordHash = passwordEncoder.encode(rawPassword);
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .organization(organization)
                    .faculty(faculty)
                    .department(department)
                    .roles(new HashSet<>(Collections.singletonList(role)))
                    .build();
            userRepository.save(user);
        }
    }

    private void createPolicyIfNotExist(Permission permission, Department allowedDepartment, Faculty allowedFaculty, String effect) {
        Policy policy = policyRepository.findByPermission(permission);
        if (policy == null) {
            policy = Policy.builder()
                    .id(UUID.randomUUID())
                    .permission(permission)
                    .allowedDepartment(allowedDepartment)
                    .allowedFaculty(allowedFaculty)
                    .effect(effect)
                    .build();
            policyRepository.save(policy);
        }
    }
}
