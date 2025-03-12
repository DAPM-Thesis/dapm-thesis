package com.dapm.security_service.models.dtos.peer;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String username;
    private String faculty;
    private String department;
    private String organization;
    private String role;
    private String permissions;
}