package com.videoflix.subscriptions_microservice.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUpdateRequestTest {

    private static Validator validator; // Instance du validateur Jakarta Bean Validation

    @BeforeAll
    static void setUpValidator() {
        // Initialise le ValidatorFactory et crée un Validator au démarrage des tests
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Test pour vérifier que les contraintes de validation sont appliquées
    // correctement pour un nom d'utilisateur valide
    @Test
    void adminUpdateRequest_validUsername_shouldPassValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un nom d'utilisateur
        // valide
        AdminUpdateRequest request = new AdminUpdateRequest("validUser", 1L);
        // WHEN : Validation de l'objet en utilisant le validateur
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'aucune violation de contrainte n'a été trouvée
        assertTrue(violations.isEmpty(), "Aucune violation ne devrait être présente pour un nom d'utilisateur valide.");
    }

    // Test pour vérifier la contrainte @NotBlank sur le nom d'utilisateur (nom
    // d'utilisateur vide)
    @Test
    void adminUpdateRequest_emptyUsername_shouldFailNotBlankValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un nom d'utilisateur vide
        AdminUpdateRequest request = new AdminUpdateRequest("", 1L);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'une violation de contrainte est présente
        assertEquals(1, violations.size(),
                "Une violation (NotBlank) devrait être présente pour un nom d'utilisateur vide.");
        // AND : Vérification que le message de la violation correspond à celui défini
        // dans l'annotation
        assertEquals("Le nom d'utilisateur ne peut pas être vide.", violations.iterator().next().getMessage(),
                "Le message de la violation NotBlank est incorrect.");
    }

    // Test pour vérifier la contrainte @Size sur le nom d'utilisateur (nom
    // d'utilisateur trop court)
    @Test
    void adminUpdateRequest_shortUsername_shouldFailSizeMinValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un nom d'utilisateur trop
        // court
        AdminUpdateRequest request = new AdminUpdateRequest("ab", 1L);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'une violation de contrainte est présente
        assertEquals(1, violations.size(),
                "Une violation (Size - min) devrait être présente pour un nom d'utilisateur trop court.");
        // AND : Vérification que le message de la violation correspond
        assertEquals("Le nom d'utilisateur doit avoir entre 3 et 50 caractères.",
                violations.iterator().next().getMessage(),
                "Le message de la violation Size (min) est incorrect.");
    }

    // Test pour vérifier la contrainte @Size sur le nom d'utilisateur (nom
    // d'utilisateur trop long)
    @Test
    void adminUpdateRequest_longUsername_shouldFailSizeMaxValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un nom d'utilisateur trop
        // long (plus de 50 caractères)
        String longUsername = "a".repeat(51);
        AdminUpdateRequest request = new AdminUpdateRequest(longUsername, 1L);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'une violation de contrainte est présente
        assertEquals(1, violations.size(),
                "Une violation (Size - max) devrait être présente pour un nom d'utilisateur trop long.");
        // AND : Vérification que le message de la violation correspond
        assertEquals("Le nom d'utilisateur doit avoir entre 3 et 50 caractères.",
                violations.iterator().next().getMessage(),
                "Le message de la violation Size (max) est incorrect.");
    }

    // Test pour vérifier la contrainte @Min sur l'ID du rôle (ID valide)
    @Test
    void adminUpdateRequest_validRoleId_shouldPassValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un ID de rôle valide
        AdminUpdateRequest request = new AdminUpdateRequest("validUser", 1L);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'aucune violation n'est présente
        assertTrue(violations.isEmpty(), "Aucune violation ne devrait être présente pour un ID de rôle valide.");
    }

    // Test pour vérifier la contrainte @Min sur l'ID du rôle (ID invalide,
    // inférieur à 1)
    @Test
    void adminUpdateRequest_invalidRoleId_shouldFailMinValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un ID de rôle invalide
        // (0)
        AdminUpdateRequest request = new AdminUpdateRequest("validUser", 0L);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'une violation est présente
        assertEquals(1, violations.size(),
                "Une violation (Min) devrait être présente pour un ID de rôle inférieur à 1.");
        // AND : Vérification que le message de la violation correspond
        assertEquals("L'ID du rôle doit être au moins 1.", violations.iterator().next().getMessage(),
                "Le message de la violation Min est incorrect.");
    }

    // Test avec un ID de rôle null (bien que @Min ne s'applique pas directement à
    // null, cela teste un cas potentiellement invalide)
    @Test
    void adminUpdateRequest_nullRoleId_shouldFailMinValidation() {
        // GIVEN : Création d'un objet AdminUpdateRequest avec un ID de rôle null
        AdminUpdateRequest request = new AdminUpdateRequest("validUser", null);
        // WHEN : Validation de l'objet
        Set<ConstraintViolation<AdminUpdateRequest>> violations = validator.validate(request);
        // THEN : Vérification qu'une violation est présente (ici, potentiellement une
        // autre contrainte implicite ou une gestion du null ailleurs)
        assertEquals(1, violations.size(), "Une violation devrait être présente pour un ID de rôle null.");
        // AND : Vérification du message (peut varier selon la configuration de
        // validation)
        assertEquals("L'ID du rôle doit être au moins 1.", violations.iterator().next().getMessage(),
                "Le message de la violation Min est incorrect pour un ID de rôle null.");
    }
}