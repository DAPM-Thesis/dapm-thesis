package com.dapm.security_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "resource_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceType {
    @Id
    private UUID id;
    private String name;
    private String description;
}
