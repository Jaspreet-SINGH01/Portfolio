package com.videoflix.users_microservice.dto;

import com.videoflix.users_microservice.validation.annotations.UniqueUsername;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDTO {

    @NotNull
    private Long id;

    @UniqueUsername
    private String username;

    @Email
    private String email;

    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial")
    private String password;
}
