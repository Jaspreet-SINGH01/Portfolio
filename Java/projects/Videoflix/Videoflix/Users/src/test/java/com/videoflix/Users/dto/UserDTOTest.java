package com.videoflix.Users.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.dto.UserDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDTOTest {

    private Validator validator; // Instance du validateur pour les tests de validation

    @BeforeEach
    void setUp() {
        // Initialisation du validateur avant chaque test
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void userDTO_ShouldPassValidation_WhenAllFieldsAreValid() {
        // Teste que la validation réussit lorsque tous les champs sont valides

        // Création d'une instance de UserDTO avec des valeurs valides
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        // Vérification qu'aucune violation n'est trouvée
        assertTrue(violations.isEmpty());
    }

    @Test
    void userDTO_ShouldFailValidation_WhenIdIsNull() {
        // Teste que la validation échoue lorsque l'ID est null

        // Création d'une instance de UserDTO avec un ID null
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("ne doit pas être nul", violations.iterator().next().getMessage());
    }

    @Test
    void userDTO_ShouldFailValidation_WhenEmailIsInvalid() {
        // Teste que la validation échoue lorsque l'adresse e-mail est invalide

        // Création d'une instance de UserDTO avec une adresse e-mail invalide
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("invalid-email");
        userDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("doit être une adresse email valide", violations.iterator().next().getMessage());
    }

    @Test
    void userDTO_ShouldFailValidation_WhenPasswordIsInvalid() {
        // Teste que la validation échoue lorsque le mot de passe est invalide

        // Création d'une instance de UserDTO avec un mot de passe invalide
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");

        // Validation de l'instance
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals(
                "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial",
                violations.iterator().next().getMessage());
    }

    @Test
    void userDTO_ShouldSetAndGetFields() {
        // Teste que les méthodes getter et setter fonctionnent correctement

        // Création d'une instance de UserDTO
        UserDTO userDTO = new UserDTO();

        // Définition des valeurs
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password123@");

        // Vérification des valeurs récupérées
        assertEquals(1L, userDTO.getId());
        assertEquals("testUser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Password123@", userDTO.getPassword());
    }
}