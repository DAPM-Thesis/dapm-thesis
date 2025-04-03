package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.AccessRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data

public class AccessRequestDto {

    private UUID id;

    private UserDto requester;


    private UUID resourceType;

    private String action;

    private String details;


    private Long durationInHours;


    private Instant requestTime;

    private Instant decisionTime;

    private AccessRequestStatus status;

    @PrePersist
    protected void onCreate() {
        this.requestTime = Instant.now();
    }
    public  AccessRequestDto (AccessRequest accessrequest){
        this.id=accessrequest.getId();
        UserDto userDto=new UserDto(accessrequest.getRequester());
        this.requester=userDto;
//        this.resourceType=accessrequest.getResourceType().getId();
        this.action=accessrequest.getAction().getName();
        this.details=accessrequest.getDetails();
        this.durationInHours=accessrequest.getDurationInHours();
        this.requestTime=accessrequest.getRequestTime();
        this.decisionTime=accessrequest.getDecisionTime();
        this.status=accessrequest.getStatus();

    }

}
