package com.dapm.security_service.services;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
public class TokenVerificationService {

    @Autowired
    private PublicKeysService publicKeysService;

    /**
     * @param token The JWT token string.
     * @return The organization identifier (e.g., "OrgA") whose public key validated the token.
     * @throws RuntimeException if token verification fails.
     */
    public String verifyTokenAndGetOrganization(String token) {
        Map<String, PublicKey> keys = publicKeysService.getAllPublicKeys();
        for (Map.Entry<String, PublicKey> entry : keys.entrySet()) {
            try {
                // Try to parse and validate the token with this public key.
                Jwts.parserBuilder()
                        .setSigningKey(entry.getValue())
                        .build()
                        .parseClaimsJws(token);
                // If no exception, the signature is valid.
                return entry.getKey();
            } catch (Exception e) {
                // Verification failed with this key; try the next one.
            }
        }
        throw new RuntimeException("Token verification failed: no matching organization public key found.");
    }
}
