package com.videoflix.Users.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.dto.UpdateUserDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateUserDTOTest {

    private Validator validator; // Instance du validateur pour les tests de validation

    @BeforeEach
    void setUp() {
        // Initialisation du validateur avant chaque test
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void updateUserDTO_ShouldPassValidation_WhenAllFieldsAreValid() {
        // Teste que la validation réussit lorsque tous les champs sont valides

        // Création d'une instance de UpdateUserDTO avec des valeurs valides
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("John Doe");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'aucune violation n'est trouvée
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateUserDTO_ShouldFailValidation_WhenNameIsBlank() {
        // Teste que la validation échoue lorsque le nom est vide

        // Création d'une instance de UpdateUserDTO avec un nom vide
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("Le nom ne peut pas être vide", violations.iterator().next().getMessage());
    }

    @Test
    void updateUserDTO_ShouldFailValidation_WhenNameIsTooShort() {
        // Teste que la validation échoue lorsque le nom est trop court

        // Création d'une instance de UpdateUserDTO avec un nom trop court
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("J");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("Le nom doit contenir entre 2 et 100 caractères", violations.iterator().next().getMessage());
    }

    @Test
    void updateUserDTO_ShouldFailValidation_WhenNameIsTooLong() {
        // Teste que la validation échoue lorsque le nom est trop long

        // Création d'une instance de UpdateUserDTO avec un nom trop long
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("a".repeat(101));
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("Le nom doit contenir entre 2 et 100 caractères", violations.iterator().next().getMessage());
    }

    @Test
    void updateUserDTO_ShouldFailValidation_WhenEmailIsInvalid() {
        // Teste que la validation échoue lorsque l'adresse e-mail est invalide

        // Création d'une instance de UpdateUserDTO avec une adresse e-mail invalide
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("John Doe");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("invalid-email");
        updateUserDTO.setPassword("Password123@");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals("L'adresse e-mail doit être valide", violations.iterator().next().getMessage());
    }

    @Test
    void updateUserDTO_ShouldFailValidation_WhenPasswordIsInvalid() {
        // Teste que la validation échoue lorsque le mot de passe est invalide

        // Création d'une instance de UpdateUserDTO avec un mot de passe invalide
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("John Doe");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("password");

        // Validation de l'instance
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        // Vérification qu'une violation est trouvée
        assertEquals(1, violations.size());

        // Vérification du message d'erreur
        assertEquals(
                "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial",
                violations.iterator().next().getMessage());
    }

    @Test
    void updateUserDTO_ShouldSetAndGetFields() {
        // Teste que les méthodes getter et setter fonctionnent correctement

        // Création d'une instance de UpdateUserDTO
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();

        // Définition des valeurs
        updateUserDTO.setName("John Doe");
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("john.doe@example.com");
        updateUserDTO.setPassword("Password123@");

        // Vérification des valeurs récupérées
        assertEquals("John Doe", updateUserDTO.getName());
        assertEquals("johndoe", updateUserDTO.getUsername());
        assertEquals("john.doe@example.com", updateUserDTO.getEmail());
        assertEquals("Password123@", updateUserDTO.getPassword());
    }
}