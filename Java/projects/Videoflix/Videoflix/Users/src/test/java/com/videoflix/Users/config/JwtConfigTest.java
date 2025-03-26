package com.videoflix.Users.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.videoflix.users_microservice.config.JwtConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest 
@EnableConfigurationProperties(JwtConfig.class) // Active les propriétés de configuration
@TestPropertySource(properties = {
        "application.jwt.secret=testSecret",
        "application.jwt.expiration=3600000"
}) // Définit les propriétés de test

class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig; // Injection de l'instance JwtConfig

    @Test
    void jwtConfig_ShouldLoadPropertiesCorrectly() {
        // Vérification que les propriétés sont correctement chargées
        assertEquals("testSecret", jwtConfig.getSecret()); // Vérification de la propriété secret
        assertEquals(3600000, jwtConfig.getExpiration()); // Vérification de la propriété expiration
    }
}