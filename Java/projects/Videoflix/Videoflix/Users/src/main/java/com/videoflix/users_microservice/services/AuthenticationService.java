package com.videoflix.users_microservice.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.videoflix.users_microservice.config.JwtConfig;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.AuthenticationException;
import com.videoflix.users_microservice.repositories.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    public String authenticate(String username, String password) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect.");
        }

        return generateToken(user);
    }

    private String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
