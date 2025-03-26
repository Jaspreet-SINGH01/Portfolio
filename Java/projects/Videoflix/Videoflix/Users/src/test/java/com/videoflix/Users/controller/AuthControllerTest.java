package com.videoflix.Users.controller;

import com.videoflix.users_microservice.dto.LoginRequest;
import com.videoflix.users_microservice.services.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLogin_SuccessfulAuthentication() throws Exception {
        // Définition du token attendu pour un login réussi
        String expectedToken = "eyJhbGciOiJIUzI1NiJ9.testToken";

        // Configuration du comportement simulé du service d'authentification
        when(authenticationService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword())).thenReturn(expectedToken);

        // Exécution de la requête de login et vérifications
        mockMvc.perform(post("/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        // Vérification de l'appel au service d'authentification
        verify(authenticationService).authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword());
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Configuration du service pour simuler une authentification échouée
        when(authenticationService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword())).thenThrow(new RuntimeException("Authentification échouée"));

        // Exécution de la requête de login et vérification de l'erreur
        mockMvc.perform(post("/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_MissingCredentials() throws Exception {
        // Création d'une requête avec des champs vides
        LoginRequest emptyRequest = new LoginRequest();
        emptyRequest.setUsername("");
        emptyRequest.setPassword("");

        // Exécution de la requête de login et vérification de l'erreur
        mockMvc.perform(post("/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }
}