package com.dapm.security_service.security;

import com.dapm.security_service.repositories.PipelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

@Component("pipelineAccessEvaluator")
public class PipelineAccessEvaluator {

    @Autowired
    private PipelineRepository pipelineRepository;

    public boolean hasPermission(String pipelineName, Authentication auth, String permission) {
        var pipeline = pipelineRepository.findByName(pipelineName)
                .orElse(null);
        if (pipeline == null || pipeline.getProject() == null) {
            return false;
        }

        String projectName = pipeline.getProject().getName();
        String requiredAuthority = permission+":" + projectName;
        System.out.println("hey there "+ permission+":" + projectName);

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredAuthority));
    }
}

