package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.models.User;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.AccessRequestRepository;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/peer/api/request")
public class RequestController {
    @Autowired
    private AccessRequestRepository requestRepository;

    @PutMapping("/{id}")
    public AccessRequestStatus updateUser(@PathVariable UUID id, @RequestHeader Authorization token) {
        // verify token
        // // Org trust

        // find token
        var request = requestRepository.findById(id);

        // match the requester of request to token

        // if matched return request

        return AccessRequestStatus.PENDING;
    }
}
