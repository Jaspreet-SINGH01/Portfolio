package com.videoflix.users_microservice.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.videoflix.users_microservice.validation.validators.UniqueUsernameValidator;

/**
 * Annotation personnalisée pour valider l'unicité du nom d'utilisateur.
 * Cette annotation est utilisée pour s'assurer qu'un nom d'utilisateur n'est
 * pas déjà utilisé dans la base de données.
 */
@Target({ ElementType.FIELD }) // L'annotation peut être appliquée aux champs.
@Retention(RetentionPolicy.RUNTIME) // L'annotation est conservée au runtime pour être accessible par réflexion.
@Constraint(validatedBy = UniqueUsernameValidator.class) // Spécifie le validateur utilisé pour cette contrainte.
public @interface UniqueUsername {

    /**
     * Le message d'erreur à afficher si la validation échoue.
     *
     * @return Le message d'erreur.
     */
    String message() default "Nom d'utilisateur déjà utilisé";

    /**
     * Les groupes de validation auxquels cette contrainte appartient.
     * Les groupes permettent d'appliquer différentes validations en fonction du
     * contexte.
     *
     * @return Un tableau de classes représentant les groupes de validation.
     */
    Class<?>[] groups() default {};

    /**
     * Les payloads qui peuvent être utilisés pour transporter des informations
     * supplémentaires sur la validation.
     *
     * @return Un tableau de classes représentant les payloads.
     */
    Class<? extends Payload>[] payload() default {};
}