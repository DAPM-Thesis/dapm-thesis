package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ProcessingElementDto {
    // Temporary ID for design purposes. This will be ignored when saving.
    private UUID id;
    // The template ID (e.g., "pe_filter" or "pe_discovery")
    private String nodeId;
    // The owner organization as a string ("OrgA" or "OrgB")
    private String ownerOrganization;
    // Inputs for the processing element.
    private Set<String> inputs;
    // Outputs for the processing element.
    private Set<String> outputs;

    public  ProcessingElementDto(){}
    public ProcessingElementDto(ProcessingElement processingElement){
        this.id=processingElement.getId();
        this.nodeId= processingElement.getNodeId();
        this.ownerOrganization=processingElement.getOwnerOrganization().getName();


        this.inputs = processingElement.getInputs() != null
                ? processingElement.getInputs().stream().collect(Collectors.toSet())
                : Collections.emptySet();

        this.outputs = processingElement.getOutputs() != null
                ? processingElement.getOutputs().stream().collect(Collectors.toSet())
                : Collections.emptySet();

    }
}
