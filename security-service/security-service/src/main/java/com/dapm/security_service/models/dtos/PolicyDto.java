package com.dapm.security_service.models.dtos;

import java.util.UUID;
import com.dapm.security_service.models.Policy;
import lombok.Data;



@Data
public class PolicyDto {

    private UUID id;

    private String permission;


    private String allowedDepartment;


    private String allowedFaculty;

    // "ALLOW" or "DENY"
    private String effect;

    public PolicyDto(Policy p){
        this.id=p.getId();
        this.permission=p.getPermission().getName();
        this.allowedDepartment=p.getAllowedDepartment().getName();
        this.allowedFaculty=p.getAllowedFaculty().getName();
        this.effect=p.getEffect();

    }
}
