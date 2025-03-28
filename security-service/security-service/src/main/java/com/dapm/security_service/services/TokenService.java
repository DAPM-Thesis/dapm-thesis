package com.dapm.security_service.services;

import com.dapm.security_service.models.Role;
import com.dapm.security_service.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private PrivateKey privateKey;

    @Value("${org.private-key}")
    private String privateKeyString;

    @PostConstruct
    public void init() {
        try {
            String privateKeyPEM = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to initialize RSA private key: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid private key format: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during private key initialization: " + e.getMessage(), e);
        }
    }


    /*
    @PostConstruct
    public void init() throws Exception {
        // Remove PEM header/footer and whitespace.
        String privateKeyPEM = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(keySpec);
    }

     */

    /**
     * Generates a JWT for the given user including claims for username,
     * organization, faculty, department, and roles.
     * default jwt token expiration time (1 hour = 3600000 ms)
     *
     * @param user the authenticated user
     * @param expirationMillis token expiration time in milliseconds
     * @return a signed JWT as a String
     */
    public String generateTokenForUser(User user, long expirationMillis) {
        Instant now = Instant.now();

        // Build claims from the user model.
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("organization", user.getOrganization().getName());
        claims.put("faculty", user.getFaculty().getName());
        claims.put("department", user.getDepartment().getName());
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));

        // Build and sign the JWT.
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
