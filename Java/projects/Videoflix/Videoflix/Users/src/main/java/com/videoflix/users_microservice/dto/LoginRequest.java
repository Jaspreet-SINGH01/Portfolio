package com.videoflix.users_microservice.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Nom d'utilisateur requis")
    private String username;

    @NotBlank(message = "Mot de passe requis")
    private String password;

    // Constructeur par défaut
    public LoginRequest() {
    }

    // Constructeur avec paramètres
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Méthode toString() pour le débogage
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                '}';
    }
}