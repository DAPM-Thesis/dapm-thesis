package com.dapm.security_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineDesignDto {
    private String name;
    private String description;
    // List of processing element designs.
    private List<ProcessingElementDto> processingElements;
    // Channels defined as lists of processing element IDs.
    private List<List<String>> channels;
}
