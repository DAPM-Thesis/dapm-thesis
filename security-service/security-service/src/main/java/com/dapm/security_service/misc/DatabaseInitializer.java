package com.dapm.security_service.misc;

import com.dapm.security_service.models.*;
import com.dapm.security_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    // Repositories
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private FacultyRepository facultyRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PolicyRepository policyRepository;
    @Autowired private PipelineRepository pipelineRepository;
    @Autowired
    private ProcessingElementRepository processingElementRepository;


    // BCrypt encoder
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Default organization name from properties.
    @Value("${dapm.defaultOrgName}")
    private String orgName;

    // --- Static UUID Definitions (unique) ---
    // Organizations
    private static final UUID ORG_A_ID = UUID.fromString("3430e05b-3b59-48c2-ae8a-22a9a9232f18");
    private static final UUID ORG_B_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    // Faculty and Department for OrgA
    private static final UUID FACULTY_CS_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DEPT_SE_ID    = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // Permissions
    private static final UUID PERM_APPROVE_ACCESS_ID         = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
    private static final UUID PERM_SET_LIMITS_ID             = UUID.fromString("bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb");
    private static final UUID PERM_READ_PIPELINE_ID          = UUID.fromString("cccccccc-3333-3333-3333-cccccccccccc");
    private static final UUID PERM_EXECUTE_PIPELINE_ID       = UUID.fromString("dddddddd-4444-4444-4444-dddddddddddd");
    private static final UUID PERM_REQUEST_ACCESS_ID         = UUID.fromString("eeeeeeee-5555-5555-5555-eeeeeeeeeeee");
    private static final UUID PERM_CONFIGURE_CROSS_ORG_TRUST_ID = UUID.fromString("ffffffff-6666-6666-6666-ffffffffffff");
    private static final UUID PERM_EXCHANGE_PUBLIC_KEYS_ID     = UUID.fromString("11111111-7777-7777-7777-111111117777");
    private static final UUID PERM_ROLE_MANAGEMENT_ID         = UUID.fromString("22222222-8888-8888-8888-222222228888");
    private static final UUID PERM_CREATE_PIPELINE_ID         = UUID.fromString("33333333-9999-9999-9999-333333339999");
    private static final UUID PERM_UPLOAD_RESOURCE_ID         = UUID.fromString("44444444-aaaa-aaaa-aaaa-44444444aaaa");
    private static final UUID PERM_DELETE_RESOURCE_ID         = UUID.fromString("55555555-bbbb-bbbb-bbbb-55555555bbbb");
    private static final UUID PERM_READ_RESOURCE_ID           = UUID.fromString("66666666-cccc-cccc-cccc-66666666cccc");
    private static final UUID PERM_EDIT_RESOURCE_ID           = UUID.fromString("77777777-dddd-dddd-dddd-77777777dddd");
    private static final UUID PERM_DOWNLOAD_RESOURCE_ID       = UUID.fromString("88888888-eeee-eeee-eeee-88888888eeee");
    private static final UUID PERM_ACCESS_RESOURCE_ID         = UUID.fromString("aaaaaaa0-0000-0000-0000-aaaaaaaa0001");
    private static final UUID PERM_MODIFY_RESOURCE_ID         = UUID.fromString("bbbbbbbb-0000-0000-0000-bbbbbbbb0001");

    // Roles for OrgA
    private static final UUID ROLE_ADMIN_ID        = UUID.fromString("cccccccc-1111-1111-1111-cccccccc1111");
    private static final UUID ROLE_DEPHEAD_ID      = UUID.fromString("dddddddd-2222-2222-2222-dddddddd2222");
    private static final UUID ROLE_RESEARCHER_ID   = UUID.fromString("eeeeeeee-3333-3333-3333-eeeeeeee3333");
    private static final UUID ROLE_PIPELINE_ID     = UUID.fromString("f17a2042-f9c8-4a46-83fc-5c83e1cb7aee"); // Given

    // Roles for OrgB
    private static final UUID ROLE_ADMIN_B_ID      = UUID.fromString("bbbbbbbb-1111-1111-1111-bbbbbbbb1111");
    private static final UUID ROLE_DEPHEAD_B_ID    = UUID.fromString("cccccccc-2222-2222-2222-cccccccc2222");
    private static final UUID ROLE_RESEARCHER_B_ID = UUID.fromString("dddddddd-3333-3333-3333-dddddddd3333");
    private static final UUID ROLE_PIPELINE_B_ID   = UUID.fromString("eeeeeeee-4444-4444-4444-eeeeeeee4444");

    // Users for OrgA
    private static final UUID USER_ANNA_ID     = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID USER_ANTHONI_ID  = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private static final UUID USER_ALICE_ID    = UUID.fromString("11111111-1111-1111-1111-111111111114");
    private static final UUID USER_ASHLEY_ID   = UUID.fromString("11111111-1111-1111-1111-111111111115");

    // Users for OrgB
    private static final UUID USER_BRIAN_ID    = UUID.fromString("22222222-2222-2222-2222-222222222223");
    private static final UUID USER_BARNI_ID    = UUID.fromString("22222222-2222-2222-2222-222222222224");
    private static final UUID USER_BOB_ID      = UUID.fromString("22222222-2222-2222-2222-222222222225");
    private static final UUID USER_BOBBY_ID    = UUID.fromString("22222222-2222-2222-2222-222222222226");

    // Created by static value
    private static final UUID CREATED_BY_ID    = UUID.fromString("6ef33aec-e030-4a67-9df2-3d11e8289fb9");

    // Pipeline ID
    private static final UUID PIPELINE_ID      = UUID.fromString("44444444-4444-4444-4444-444444444444");

    // Node IDs
    private static final UUID NODE_A1_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID NODE_A2_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID NODE_B_ID  = UUID.fromString("77777777-7777-7777-7777-777777777777");

    // Policy for EXECUTE_PIPELINE
    private static final UUID POLICY_EXEC_PIPELINE_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff1");

    // Resources and Resource Types
    // Resource Type for OrgA
    private static final UUID RESOURCETYPE_A_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    // Resource Type for OrgB
    private static final UUID RESOURCETYPE_B_ID = UUID.fromString("66666666-7777-8888-9999-aaaaaaaaaaaa");

    // Resources for OrgA
    private static final UUID RESOURCE_A_ID = UUID.fromString("aaaaaaa0-1111-1111-1111-aaaaaaaaaaaa");
    private static final UUID RESOURCE_B_ID = UUID.fromString("aaaaaaa1-3333-3333-3333-aaaaaaaaaaaa");
    // Resource for OrgB
    private static final UUID RESOURCE_C_ID = UUID.fromString("ccccccc0-5555-5555-5555-cccccccccccc");

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Default Org Name: " + orgName);

        Organization org;


//TODO: orgB IS  hardcoded , chnage later to be dynamic
        if (orgName.equals("OrgA")) {
            org = organizationRepository.findByName("OrgA")
                    .orElseGet(() -> organizationRepository.save(
                            Organization.builder()
                                    .id(ORG_A_ID)
                                    .name("OrgA")
                                    .build()
                            ));
//            organizationRepository.findByName("OrgB")
//                                    .orElseGet(() -> organizationRepository.save(
//                                            Organization.builder()
//                                                    .id(ORG_B_ID)
//                                                    .name("OrgB")
//                                                    .build()
//                    ));
        } else {
            org = organizationRepository.findByName("OrgB")
                    .orElseGet(() -> organizationRepository.save(
                            Organization.builder()
                                    .id(ORG_B_ID)
                                    .name("OrgB")
                                    .build()
                    ));
            // 👇 Add this only inside the OrgB block
            ProcessingElement peB = ProcessingElement.builder()
                    .id(NODE_B_ID)
                    .templateId("pe_discovery")
                    .ownerOrganization(org)
                    .inputs(Set.of("Event"))
                    .outputs(Set.of("Model"))
                    .visibility(Set.of("OrgA"))
                    .build();

            processingElementRepository.save(peB);

        }



        // 2. Create Faculty "Computer Science" for OrgA.
        Faculty faculty = facultyRepository.findByName("Computer Science")
                .orElseGet(() -> {
                    Faculty newFaculty = Faculty.builder()
                            .id(FACULTY_CS_ID)
                            .name("Computer Science")
                            .organization(org)
                            .build();
                    return facultyRepository.save(newFaculty);
                });

// 3. Create Department "Software Engineering" under "Computer Science".
        Department department = departmentRepository.findByName("Software Engineering")
                .orElseGet(() -> {
                    Department newDept = Department.builder()
                            .id(DEPT_SE_ID)
                            .name("Software Engineering")
                            .faculty(faculty)
                            .build();
                    return departmentRepository.save(newDept);
                });


        // 4. Define Permissions.
        Map<String, Permission> permissionMap = new HashMap<>();
        permissionMap.put("APPROVE_ACCESS", createPermissionIfNotExistStatic("APPROVE_ACCESS", "Approve access requests", PERM_APPROVE_ACCESS_ID));
        permissionMap.put("SET_LIMITS", createPermissionIfNotExistStatic("SET_LIMITS", "Set resource usage limits", PERM_SET_LIMITS_ID));
        permissionMap.put("READ_PIPELINE", createPermissionIfNotExistStatic("READ_PIPELINE", "Read pipeline details", PERM_READ_PIPELINE_ID));
        permissionMap.put("EXECUTE_PIPELINE", createPermissionIfNotExistStatic("EXECUTE_PIPELINE", "Execute pipeline", PERM_EXECUTE_PIPELINE_ID));
        permissionMap.put("REQUEST_ACCESS", createPermissionIfNotExistStatic("REQUEST_ACCESS", "Request access", PERM_REQUEST_ACCESS_ID));
        permissionMap.put("CONFIGURE_CROSS_ORG_TRUST", createPermissionIfNotExistStatic("CONFIGURE_CROSS_ORG_TRUST", "Configure cross organization trust policies", PERM_CONFIGURE_CROSS_ORG_TRUST_ID));
        permissionMap.put("EXCHANGE_PUBLIC_KEYS", createPermissionIfNotExistStatic("EXCHANGE_PUBLIC_KEYS", "Exchange public keys", PERM_EXCHANGE_PUBLIC_KEYS_ID));
        permissionMap.put("ROLE_MANAGEMENT", createPermissionIfNotExistStatic("ROLE_MANAGEMENT", "Manage roles", PERM_ROLE_MANAGEMENT_ID));
        permissionMap.put("CREATE_PIPELINE", createPermissionIfNotExistStatic("CREATE_PIPELINE", "Create new pipeline", PERM_CREATE_PIPELINE_ID));
        permissionMap.put("UPLOAD_RESOURCE", createPermissionIfNotExistStatic("UPLOAD_RESOURCE", "Upload resource", PERM_UPLOAD_RESOURCE_ID));
        permissionMap.put("DELETE_RESOURCE", createPermissionIfNotExistStatic("DELETE_RESOURCE", "Delete resource", PERM_DELETE_RESOURCE_ID));
        permissionMap.put("READ_RESOURCE", createPermissionIfNotExistStatic("READ_RESOURCE", "Read resource", PERM_READ_RESOURCE_ID));
        permissionMap.put("EDIT_RESOURCE", createPermissionIfNotExistStatic("EDIT_RESOURCE", "Edit resource", PERM_EDIT_RESOURCE_ID));
        permissionMap.put("DOWNLOAD_RESOURCE", createPermissionIfNotExistStatic("DOWNLOAD_RESOURCE", "Download resource", PERM_DOWNLOAD_RESOURCE_ID));
        permissionMap.put("ACCESS_RESOURCE", createPermissionIfNotExistStatic("ACCESS_RESOURCE", "Access resource", PERM_ACCESS_RESOURCE_ID));
        permissionMap.put("MODIFY_RESOURCE", createPermissionIfNotExistStatic("MODIFY_RESOURCE", "Modify resource", PERM_MODIFY_RESOURCE_ID));

        // 5. Create Roles for OrgA.
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
        Role adminRole = createRoleIfNotExistStatic("ADMIN", org, adminPerms, ROLE_ADMIN_ID);

        Set<Permission> depHeadPerms = new HashSet<>(Arrays.asList(
                permissionMap.get("APPROVE_ACCESS"),
                permissionMap.get("SET_LIMITS"),
                permissionMap.get("READ_PIPELINE"),
                permissionMap.get("EXECUTE_PIPELINE"),
                permissionMap.get("REQUEST_ACCESS")
        ));
        Role depHeadRole = createRoleIfNotExistStatic("DEPARTMENT_HEAD", org, depHeadPerms, ROLE_DEPHEAD_ID);

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
        Role researcherRole = createRoleIfNotExistStatic("RESEARCHER", org, researcherPerms, ROLE_RESEARCHER_ID);

        Set<Permission> pipelinePerms = new HashSet<>(Arrays.asList(
                permissionMap.get("READ_RESOURCE"),
                permissionMap.get("ACCESS_RESOURCE"),
                permissionMap.get("MODIFY_RESOURCE"),
                permissionMap.get("DELETE_RESOURCE")
        ));
        Role pipelineRole = createRoleIfNotExistStatic("PIPELINE_ROLE", org, pipelinePerms, ROLE_PIPELINE_ID);

        // Create Roles for OrgB.
//        Role adminRoleB = createRoleIfNotExistStatic("ADMIN", orgB, adminPerms, ROLE_ADMIN_B_ID);
//        Role depHeadRoleB = createRoleIfNotExistStatic("DEPARTMENT_HEAD", orgB, depHeadPerms, ROLE_DEPHEAD_B_ID);
//        Role researcherRoleB = createRoleIfNotExistStatic("RESEARCHER", orgB, researcherPerms, ROLE_RESEARCHER_B_ID);
//        Role pipelineRoleB = createRoleIfNotExistStatic("PIPELINE_ROLE", orgB, pipelinePerms, ROLE_PIPELINE_B_ID);

        // 6. Create Users for OrgA.
        createUserIfNotExistStatic("anna", "anna@example.com", "dapm", adminRole, org, faculty, department, USER_ANNA_ID);
        createUserIfNotExistStatic("anthoni", "anthoni@example.com", "dapm", depHeadRole, org, faculty, department, USER_ANTHONI_ID);
        createUserIfNotExistStatic("alice", "alice@example.com", "dapm", researcherRole, org, faculty, department, USER_ALICE_ID);
        createUserIfNotExistStatic("ashley", "ashley@example.com", "dapm", researcherRole, org, faculty, department, USER_ASHLEY_ID);

        // 7. Create Users for OrgB.
//        createUserIfNotExistStatic("brian", "brian@example.com", "dapm", adminRoleB, orgB, faculty, department, USER_BRIAN_ID);
//        createUserIfNotExistStatic("barni", "barni@example.com", "dapm", depHeadRoleB, orgB, faculty, department, USER_BARNI_ID);
//        createUserIfNotExistStatic("bob", "bob@example.com", "dapm", researcherRoleB, orgB, faculty, department, USER_BOB_ID);
//        createUserIfNotExistStatic("bobby", "bobby@example.com", "dapm", researcherRoleB, orgB, faculty, department, USER_BOBBY_ID);

        // 8. Create a Policy for EXECUTE_PIPELINE in OrgA.
        createPolicyIfNotExistStatic(permissionMap.get("EXECUTE_PIPELINE"), department, faculty, "ALLOW", POLICY_EXEC_PIPELINE_ID);



        // 10. Create a Pipeline (owned by OrgA).
        Pipeline pipeline = Pipeline.builder()
                .id(PIPELINE_ID)
                .name("Cross-Org Pipeline")
                .ownerOrganization(org)
                .description("Pipeline with processing elements from OrgA and OrgB")
                .pipelineRole(pipelineRole)
                .processingElements(new HashSet<>())  // Use processingElements field
                .tokens(new HashSet<>())
                .createdBy(CREATED_BY_ID)
                .createdAt(Instant.parse("2025-03-11T13:45:07.455Z"))
                .updatedAt(Instant.parse("2025-03-11T13:45:07.455Z"))
                .build();


        // 11. Create Processing Elements.
// You can use your existing node IDs for processing element IDs if desired,
// or generate new ones. Here, we're reusing the constants.
        ProcessingElement pe1 = ProcessingElement.builder()
                .id(NODE_A1_ID)  // or UUID.randomUUID() if you prefer
                .templateId("pe_filter")  // This template represents an OrgA template
                .ownerOrganization(org)
                .inputs(new HashSet<>())   // Set default inputs as needed
                .outputs(new HashSet<>())  // Set default outputs as needed
                .build();

        ProcessingElement pe2 = ProcessingElement.builder()
                .id(NODE_A2_ID)
                .templateId("pe_filter")
                .ownerOrganization(org)
                .inputs(new HashSet<>())
                .outputs(new HashSet<>())
                .build();

//        ProcessingElement pe3 = ProcessingElement.builder()
//                .id(NODE_B_ID)
//                .nodeId("pe_discovery")  // This template represents an OrgB template
//                .ownerOrganization(orgB)
//                .inputs(new HashSet<>())
//                .outputs(new HashSet<>())
//                .build();


//        // Associate allowed resources with nodes.
//        node1.setAllowedResources(new HashSet<>(Arrays.asList(resourceA, resourceB)));
//        node2.setAllowedResources(new HashSet<>());
//        node3.setAllowedResources(new HashSet<>(Collections.singletonList(resourceC)));

        pe1 = processingElementRepository.save(pe1);
        pe2 = processingElementRepository.save(pe2);
//        pe3 = processingElementRepository.save(pe3);

        // 12. Associate nodes with the pipeline (ManyToMany).

        // Set<Node> nodes = new HashSet<>(Arrays.asList(node1, node2, node3));
        // pipeline.setNodes(nodes);
        pipeline.getProcessingElements().clear();
//        pipeline.getProcessingElements().addAll(Arrays.asList(pe1, pe2, pe3));


        // 13. Set tokens as empty for now.
        //pipeline.setTokens(new HashSet<>());
        pipeline.getTokens().clear();

        // 14. Create sample PE Templates for testing assembly stage.




        // Save the pipeline.
        pipeline = pipelineRepository.save(pipeline);

        System.out.println("Database initialization complete.");
    }

    // --- Helper Methods ---

    private Permission createPermissionIfNotExistStatic(String name, String description, UUID id) {
        Permission permission = permissionRepository.findByName(name);
        if (permission == null) {
            permission = Permission.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .build();
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    private Role createRoleIfNotExistStatic(String name, Organization organization, Set<Permission> permissions, UUID id) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = Role.builder()
                    .id(id)
                    .name(name)
                    .organization(organization)
                    .permissions(permissions)
                    .build();
            role = roleRepository.save(role);
        }
        return role;
    }

    private void createUserIfNotExistStatic(String username, String email, String rawPassword, Role role,
                                            Organization organization, Faculty faculty, Department department, UUID id) {
        userRepository.findByUsername(username).orElseGet(() -> {
            String passwordHash = passwordEncoder.encode(rawPassword);
            User user = User.builder()
                    .id(id)
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .organization(organization)
                    .faculty(faculty)
                    .department(department)
                    .roles(new HashSet<>(Collections.singletonList(role)))
                    .build();
            return userRepository.save(user);
        });
    }


    private void createPolicyIfNotExistStatic(Permission permission, Department allowedDepartment, Faculty allowedFaculty,
                                              String effect, UUID id) {
        Policy policy = policyRepository.findByPermission(permission);
        if (policy == null) {
            policy = Policy.builder()
                    .id(id)
                    .permission(permission)
                    .allowedDepartment(allowedDepartment)
                    .allowedFaculty(allowedFaculty)
                    .effect(effect)
                    .build();
            policyRepository.save(policy);
        }
    }
}
