package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.dtos.*;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.repositories.ProjectsRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectsRolesRepository projectsRolesRepository;

    @PreAuthorize("hasAuthority('READ_PROJECT')")
    @GetMapping
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectDto::new)
                .toList();
    }
    @PreAuthorize("hasAuthority('READ_PROJECT')")
    @GetMapping("/{title}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable String title) {
        return projectRepository.findByTitle(title)
                .map(project -> ResponseEntity.ok(new ProjectDto(project)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody CreateProjectDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Project project = new Project();
        project.setId( UUID.randomUUID());
        project.setTitle(request.getTitle());
        Organization organization = userDetails.getUser().getOrganization();

        project.setOrganization(organization);

        Project created =projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(created));
    }

    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @PutMapping("/{title}/assignrole")
    public ResponseEntity<ProjectDto> assignRoleToProject(@PathVariable String title, @RequestBody ProjectRolesAssignmentDto projectRolesAssignmentDto) {
        Project project= projectRepository.findByTitle(title).orElse(null);
        ProjectRole projectRole=projectsRolesRepository.findByName(projectRolesAssignmentDto.getRole());

        project.getProjectRoles().add(projectRole);

        Project updated =projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(updated));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    @DeleteMapping("/{title}")
    public ResponseEntity<Void> deleteProject(@PathVariable String title) {
        Project project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        projectRepository.delete(project);
        return ResponseEntity.noContent().build();
    }
    //update a project with createProjectDto
    @PreAuthorize("hasAuthority('UPDATE_PROJECT')")
    @PutMapping("/{title}")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable String title,
            @RequestBody CreateProjectDto request
    ) {
        Project project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        project.setTitle(request.getTitle());
        Project updated = projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(updated));
    }



}
