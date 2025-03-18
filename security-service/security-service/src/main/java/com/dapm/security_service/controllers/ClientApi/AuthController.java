package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.services.AuthenticationService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TokenVerificationService tokenVerificationService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        String token = authenticationService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        return new AuthResponse(token);
    }

    @PostMapping("token/verify")
    public ResponseEntity<String> verifyToken(@RequestParam String token) {
        try {
            String orgId = tokenVerificationService.verifyTokenAndGetOrganization(token);
            return ResponseEntity.ok("Token matches organization: " + orgId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }
}
