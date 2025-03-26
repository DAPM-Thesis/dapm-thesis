package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.services.AuthenticationService2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public final AuthenticationService2 service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody CreateUserDto request
    ){
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> register(
        @RequestBody AuthRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }
}
