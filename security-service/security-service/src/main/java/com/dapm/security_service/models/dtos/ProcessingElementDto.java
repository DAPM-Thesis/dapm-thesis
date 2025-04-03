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
    private UUID id;
    private String templateId;
    // For simplicity, we use the organization's name. You could also use an ID if preferred.
    private String ownerOrganization;
    private Set<String> inputs;
    private Set<String> outputs;
}
