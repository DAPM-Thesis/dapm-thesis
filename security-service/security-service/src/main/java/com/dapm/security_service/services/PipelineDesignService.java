package com.dapm.security_service.services;

import com.dapm.security_service.models.dtos.PeTemplateDto;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.models.dtos.PipelineDesignDto;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.User;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.PipelineRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PipelineDesignService {

    private final PeTemplateService peTemplateService;
    private final PipelineRepository pipelineRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;  // For converting channels list to JSON

    public PipelineDesignService(PeTemplateService peTemplateService,
                                 PipelineRepository pipelineRepository,
                                 OrganizationRepository organizationRepository,
                                 UserRepository userRepository,
                                 ObjectMapper objectMapper) {
        this.peTemplateService = peTemplateService;
        this.pipelineRepository = pipelineRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Aggregates available processing element templates.
     * Converts a list of PeTemplateDto to a list of ProcessingElementDto.
     *
     * @param org the organization for which templates are fetched.
     * @return list of ProcessingElementDto.
     */
    public List<ProcessingElementDto> getAvailablePeTemplates(String org) {
        List<PeTemplateDto> peTemplates = peTemplateService.getAvailablePeTemplates(org);
        return peTemplates.stream()
                .map(template -> new ProcessingElementDto(
                        // Generate a temporary instance ID (for UI only)
                        UUID.randomUUID(),
                        // Use the template's ID as the templateId.
                        template.getId(),
                        // Owner organization as a string.
                        template.getOwner(),
                        new HashSet<>(),   // default inputs
                        new HashSet<>()    // default outputs
                ))
                .collect(Collectors.toList());
    }

    /**
     * Saves a pipeline design.
     * Maps the PipelineDesignDto (which includes processing elements and channels) to a Pipeline entity.
     * Generates new persistent IDs for processing elements and updates channel references accordingly.
     *
     * @param pipelineDesignDto the pipeline design data from the client.
     * @return the saved Pipeline entity.
     */
    public Pipeline savePipelineDesign(PipelineDesignDto pipelineDesignDto) {
        // Map to store temporary ID (as string) -> new persistent UUID.
        Map<String, UUID> idMapping = new HashMap<>();
        Set<ProcessingElement> processingElements = new HashSet<>();

        if (pipelineDesignDto.getProcessingElements() != null) {
            for (ProcessingElementDto dto : pipelineDesignDto.getProcessingElements()) {
                // Use the incoming DTO id as temporary key; if null, generate a new temporary key.
                String tempId = (dto.getId() != null) ? dto.getId().toString() : UUID.randomUUID().toString();
                // Generate a new persistent UUID for this processing element.
                UUID persistentId = UUID.randomUUID();
                idMapping.put(tempId, persistentId);

                ProcessingElement pe = ProcessingElement.builder()
                        .id(persistentId)
                        .templateId(dto.getTemplateId())
                        // Look up the owner organization by the name provided in the DTO.
                        .ownerOrganization(getOrganization(dto.getOwnerOrganization()))
                        .inputs(dto.getInputs())
                        .outputs(dto.getOutputs())
                        .build();
                processingElements.add(pe);
            }
        }

        // Update the channels list: For each channel (list of temporary IDs),
        // replace each temporary ID with the corresponding persistent ID.
        List<List<String>> updatedChannels = pipelineDesignDto.getChannels().stream()
                .map(channel -> channel.stream()
                        .map(tempId -> {
                            UUID newId = idMapping.getOrDefault(tempId, null);
                            return (newId != null) ? newId.toString() : tempId;
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // Convert the updated channels list to a JSON string.
        String channelsJson = null;
        try {
            channelsJson = objectMapper.writeValueAsString(updatedChannels);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // TODO: Optionally handle this exception properly.
        }

        // Retrieve the authenticated user's ID for the 'createdBy' field.
        UUID authenticatedUserId = getAuthenticatedUserId();

        // Retrieve the default owner organization from the database.
        Organization defaultOrg = getDefaultOwnerOrganization();

        // Create a Pipeline entity (without pipelineRole, which will be set later).
        Pipeline pipeline = Pipeline.builder()
                .id(UUID.randomUUID())
                .name(pipelineDesignDto.getName())
                .description(pipelineDesignDto.getDescription())
                .ownerOrganization(defaultOrg)
                .pipelineRole(null)  // Not set during assembly.
                .processingElements(processingElements)
                .channelsJson(channelsJson)
                .createdBy(authenticatedUserId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return pipelineRepository.save(pipeline);
    }

    /**
     * Looks up an Organization by its name.
     *
     * @param orgName the name of the organization.
     * @return the Organization object.
     */
    private Organization getOrganization(String orgName) {
        Organization org = organizationRepository.findByName(orgName);
        if (org == null) {
            throw new RuntimeException("Organization '" + orgName + "' not found in database");
        }
        return org;
    }

    /**
     * Retrieves the default owner organization from the database.
     * In this case, we look up OrgA by name.
     *
     * @return the Organization representing OrgA.
     */
    private Organization getDefaultOwnerOrganization() {
        Organization orgA = organizationRepository.findByName("OrgA");
        if (orgA == null) {
            throw new RuntimeException("Default organization OrgA not found in database");
        }
        return orgA;
    }

    /**
     * Retrieves the authenticated user's ID.
     * For now, it looks up user "alice" from the database.
     *
     * @return the UUID of the authenticated user.
     */
    private UUID getAuthenticatedUserId() {
        User alice = userRepository.findByUsername("alice");
        if (alice == null) {
            throw new RuntimeException("Default user 'alice' not found in database");
        }
        return alice.getId();
    }
}
