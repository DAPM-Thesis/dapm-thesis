package com.dapm.security_service.services;

import com.dapm.security_service.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class TokenService {
    @Value("${org.private-OrgA}")
    private String privateKeyString;

    @Value("${jwt.expiration.ms:3600000}") // default 1 hour
    private long jwtExpirationMs;

    /**
     * @param user the authenticated user
     * @return a signed JWT token as a string
     */
    public String generateToken(User user) {
        PrivateKey privateKey = getPrivateKeyFromPem(privateKeyString);

        String roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(","));

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * @param keyPem the PEM string (including header/footer)
     * @return the PrivateKey object
     */
    private PrivateKey getPrivateKeyFromPem(String keyPem) {
        try {
            String privateKeyPEM = keyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse private key", e);
        }
    }
}
