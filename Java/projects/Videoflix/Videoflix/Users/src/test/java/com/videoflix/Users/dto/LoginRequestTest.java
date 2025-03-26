package com.videoflix.Users.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.dto.LoginRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRequestTest {

    private Validator validator; // Instance du validateur pour les tests de validation

    @BeforeEach
    void setUp() {
        // Initialisation du validateur avant chaque test
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void loginRequest_ShouldPassValidation_WhenUsernameAndPasswordAreNotBlank() {
        // Teste que la validation réussit lorsque le nom d'utilisateur et le mot de
        // passe ne sont pas vides

        // Création d'une instance de LoginRequest avec des valeurs valides
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("testPassword");

        // Validation de l'instance
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Vérification qu'aucune violation n'est trouvée
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_ShouldFailValidation_WhenUsernameIsBlank() {
        // Teste que la validation échoue lorsque le nom d'utilisateur est vide

        // Création d'une instance de LoginRequest avec un nom d'utilisateur vide
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("testPassword");

        // Validation de l'instance
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("Mot de passe requis", violations.iterator().next().getMessage());
    }

    @Test
    void loginRequest_ShouldFailValidation_WhenPasswordIsBlank() {
        // Teste que la validation échoue lorsque le mot de passe est vide

        // Création d'une instance de LoginRequest avec un mot de passe vide
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("");

        // Validation de l'instance
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("Mot de passe requis", violations.iterator().next().getMessage());
    }

    @Test
    void loginRequest_ShouldFailValidation_WhenUsernameAndPasswordAreBlank() {
        // Teste que la validation échoue lorsque le nom d'utilisateur et le mot de
        // passe sont vides

        // Création d'une instance de LoginRequest avec un nom d'utilisateur et un mot
        // de passe vides
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        // Validation de l'instance
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Vérification que deux violations sont trouvées
        assertEquals(2, violations.size());

        // Vérification des messages d'erreur
        assertTrue(violations.stream().anyMatch(violation -> violation.getMessage().equals("Mot de passe requis")));
    }

    @Test
    void loginRequest_ShouldSetAndGetUsernameAndPassword() {
        // Teste que les méthodes setUsername(), getUsername(), setPassword() et
        // getPassword() fonctionnent correctement

        // Création d'une instance de LoginRequest
        LoginRequest loginRequest = new LoginRequest();

        // Définition des valeurs
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("testPassword");

        // Vérification des valeurs récupérées
        assertEquals("testUser", loginRequest.getUsername());
        assertEquals("testPassword", loginRequest.getPassword());
    }
}