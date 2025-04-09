package com.videoflix.subscriptions_microservice.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TokenHashingService {

    private final PasswordEncoder passwordEncoder;

    public TokenHashingService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hashToken(String pushToken) {
        // Le PasswordEncoder génère automatiquement un sel aléatoire et le combine avec
        // le hash
        return passwordEncoder.encode(pushToken);
    }

    public boolean verifyToken(String rawToken, String hashedTokenFromDatabase) {
        return passwordEncoder.matches(rawToken, hashedTokenFromDatabase);
    }
}