
package com.dapm.security_service.models.dtos.peer;

import lombok.Data;

import java.util.UUID;

/**
 * Sub-DTO for the requester details (User/Organization info).
 */
@Data
public class RequesterInfoDto {
    private UUID requesterId;
    private String username;
    private String organization;
    private String faculty;
    private String department;
    private String role;
    private String permissions;


}

