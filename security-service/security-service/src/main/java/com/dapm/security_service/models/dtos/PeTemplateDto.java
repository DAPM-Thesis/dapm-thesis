package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.PeTemplate;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeTemplateDto {
    private String id;            // e.g., "pe_alignments"
    private String name;
    private String description;
    private String owner;         // "OrgA" or "OrgB"
    private Set<String> visibility;
}
