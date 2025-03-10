package com.dapm.security_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @ManyToOne
    @JoinColumn(name = "allowed_department_id")
    private Department allowedDepartment;

    @ManyToOne
    @JoinColumn(name = "allowed_faculty_id")
    private Faculty allowedFaculty;

    // "ALLOW" or "DENY"
    private String effect;
}
