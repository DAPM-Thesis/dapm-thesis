package com.dapm.security_service.services;

import com.dapm.security_service.models.User;
import com.dapm.security_service.repositories.interfaces.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * @param username the username
     * @param rawPassword the raw (plaintext) password
     * @return a JWT token string
     */
    public String authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        // jwt token expiration time (1 hour = 3600000 ms)
        return tokenService.generateTokenForUser(user, 3600000);
    }
}
