package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "processing_element")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingElement {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // The organization that owns this processing element.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

    // The identifier of the template used for this processing element.
    @Column(name = "template_id", nullable = false)
    private String templateId;

    // A set of input types, e.g., ["Event", "PetriNet"]
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_inputs", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "input")
    @Builder.Default
    private Set<String> inputs = new HashSet<>();

    // A set of output types, e.g., ["Alignment"]
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_outputs", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "output")
    @Builder.Default
    private Set<String> outputs = new HashSet<>();
}
