package com.dapm.security_service.services;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.PipelineDesignDto;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.PipelineRepository;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PipelineDesignService {

    private final PipelineRepository pipelineRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProcessingElementRepository processingElementRepository;
    private final ObjectMapper objectMapper;
    private final VisiblePeClient visiblePeClient;

    public PipelineDesignService(
            PipelineRepository pipelineRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ProcessingElementRepository processingElementRepository,
            ObjectMapper objectMapper,
            VisiblePeClient visiblePeClient
    ) {
        this.pipelineRepository = pipelineRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.processingElementRepository = processingElementRepository;
        this.objectMapper = objectMapper;
        this.visiblePeClient = visiblePeClient;
    }

    public List<ProcessingElementDto> getAvailablePeTemplates(String org) {
        List<ProcessingElementDto> localDtos = processingElementRepository
                .findByOwnerOrganization_NameOrVisibilityContaining(org, org)
                .stream()
                .map(ProcessingElementDto::new)
                .collect(Collectors.toList());

        List<ProcessingElementDto> remoteDtos = visiblePeClient.getVisiblePEsFromOrgB(org);

        Set<UUID> existingIds = localDtos.stream()
                .map(ProcessingElementDto::getId)
                .collect(Collectors.toSet());

        remoteDtos.stream()
                .filter(dto -> dto.getId() != null && !existingIds.contains(dto.getId()))
                .forEach(localDtos::add);

        return localDtos;
    }

    public Pipeline savePipelineDesign(PipelineDesignDto dto) {
        Map<String, UUID> idMap = new HashMap<>();
        Set<ProcessingElement> elements = new HashSet<>();

        if (dto.getProcessingElements() != null) {
            for (ProcessingElementDto peDto : dto.getProcessingElements()) {
                String tempId = peDto.getId() != null ? peDto.getId().toString() : UUID.randomUUID().toString();
                UUID newId = UUID.randomUUID();
                idMap.put(tempId, newId);

                elements.add(ProcessingElement.builder()
                        .id(newId)
                        .templateId(peDto.getTemplateId())
                        .ownerOrganization(getOrganization(peDto.getOwnerOrganization()))
                        .inputs(peDto.getInputs())
                        .outputs(peDto.getOutputs())
                        .build());
            }
        }

        List<List<String>> updatedChannels = dto.getChannels().stream()
                .map(channel -> channel.stream()
                        .map(tempId -> Optional.ofNullable(idMap.get(tempId)).map(UUID::toString).orElse(tempId))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        String channelsJson = "";
        try {
            channelsJson = objectMapper.writeValueAsString(updatedChannels);
        } catch (Exception ignored) {}

        Pipeline pipeline = Pipeline.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .description(dto.getDescription())
                .ownerOrganization(getDefaultOwnerOrganization())
                .pipelineRole(null)
                .processingElements(elements)
                .channelsJson(channelsJson)
                .createdBy(getAuthenticatedUserId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return pipelineRepository.save(pipeline);
    }

    private Organization getOrganization(String name) {
        return organizationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Organization '" + name + "' not found"));
    }

    private Organization getDefaultOwnerOrganization() {
        return getOrganization("OrgA");
    }

    private UUID getAuthenticatedUserId() {
        return userRepository.findByUsername("alice")
                .orElseThrow(() -> new RuntimeException("Default user 'alice' not found"))
                .getId();
    }
}
