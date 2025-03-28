package com.videoflix.users_microservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.videoflix.users_microservice.dto.LoginRequest;
import com.videoflix.users_microservice.services.AuthenticationService;

@RestController
@RequestMapping("/users/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Constructeur pour AuthController.
     * Injecte le service AuthenticationService.
     *
     * @param authenticationService Le service d'authentification.
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint pour la connexion d'un utilisateur.
     * Reçoit une requête POST avec les informations de connexion (nom d'utilisateur
     * et mot de passe)
     * dans le corps de la requête (LoginRequest).
     *
     * @param loginRequest Les informations de connexion de l'utilisateur.
     * @return Un token JWT en cas de connexion réussie.
     */
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        // Appelle le service d'authentification pour authentifier l'utilisateur et
        // obtenir un token JWT.
        return authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
    }
}