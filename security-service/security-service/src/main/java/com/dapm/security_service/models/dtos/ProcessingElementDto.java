package com.dapm.security_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingElementDto {
    // Temporary ID for design purposes. This will be ignored when saving.
    private UUID id;
    // The template ID (e.g., "pe_filter" or "pe_discovery")
    private String templateId;
    // The owner organization as a string ("OrgA" or "OrgB")
    private String ownerOrganization;
    // Inputs for the processing element.
    private Set<String> inputs;
    // Outputs for the processing element.
    private Set<String> outputs;
}
