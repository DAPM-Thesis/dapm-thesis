package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pe_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeTemplate {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id; // e.g., "pe_alignments"

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // The organization that owns this template (e.g., OrgA or OrgB)
    @Column(name = "owner", nullable = false)
    private String owner;

    // Organizations that are allowed to view this template.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pe_template_visibility", joinColumns = @JoinColumn(name = "pe_template_id"))
    @Column(name = "visible_org")
    private Set<String> visibility = new HashSet<>();
}
