package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.dtos.ProjectDto;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.models.dtos.CreateProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody CreateProjectDto request,
            CustomUserDetails userDetails
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
}
