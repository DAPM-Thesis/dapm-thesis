package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Channel;
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

    private List<ProcessingElementDto> processingElements;

    private List<List<String>> channels;

}
