package com.dapm.security_service.models.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ApproveNodeRequestDto {
    private UUID requestId;
    private Integer allowedDurationHours;
    private Integer allowedNoExecutions;
    private Integer allowedDataUsagePercentage;
}
