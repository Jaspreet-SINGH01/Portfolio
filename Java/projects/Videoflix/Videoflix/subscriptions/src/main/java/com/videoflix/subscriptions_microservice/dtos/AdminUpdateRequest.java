package com.videoflix.subscriptions_microservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateRequest {

    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide.")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit avoir entre 3 et 50 caractères.")
    private String username;

    @Min(value = 1, message = "L'ID du rôle doit être au moins 1.")
    private Long roleId;

    public AdminUpdateRequest(String username, Long roleId) {
        this.username = username;
        this.roleId = roleId;
    }
}