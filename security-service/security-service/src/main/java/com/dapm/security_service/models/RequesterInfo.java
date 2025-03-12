package com.dapm.security_service.models;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
public class RequesterInfo {
    private UUID requesterId;
    private String username;
    private String faculty;
    private String department;
    private String organization;
    private String role;
    private String permissions;
}
