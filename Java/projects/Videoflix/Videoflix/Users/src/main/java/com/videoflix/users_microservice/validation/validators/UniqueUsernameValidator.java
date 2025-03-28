package com.videoflix.users_microservice.validation.validators;

import com.videoflix.users_microservice.repositories.UserRepository;
import com.videoflix.users_microservice.validation.annotations.UniqueUsername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validateur personnalisé pour l'annotation UniqueUsername.
 * Cette classe implémente l'interface ConstraintValidator et est utilisée pour
 * vérifier si un nom d'utilisateur est unique dans la base de données.
 */
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository userRepository;

    /**
     * Constructeur pour UniqueUsernameValidator.
     * Injecte le UserRepository pour accéder à la base de données des utilisateurs.
     *
     * @param userRepository Le repository pour accéder aux données des
     *                       utilisateurs.
     */
    public UniqueUsernameValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Méthode pour vérifier si un nom d'utilisateur est valide (unique).
     *
     * @param username Le nom d'utilisateur à valider.
     * @param context  Le contexte de validation.
     * @return true si le nom d'utilisateur est unique, false sinon.
     */
    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        // Vérifie si un utilisateur avec le nom d'utilisateur donné existe déjà dans la
        // base de données.
        // Si la requête renvoie une liste vide, cela signifie que le nom d'utilisateur
        // est unique.
        return userRepository.findByUsername(username).isEmpty();
    }
}