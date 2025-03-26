package com.videoflix.Users.validation;

import com.videoflix.users_microservice.validation.annotations.UniqueUsername;
import com.videoflix.users_microservice.validation.validators.UniqueUsernameValidator;
import jakarta.validation.Constraint;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueUsernameTest {

    @Test
    void uniqueUsernameAnnotation_ShouldHaveCorrectAttributes() {
        // Récupération de l'annotation UniqueUsername
        UniqueUsername annotation = UniqueUsername.class.getAnnotation(UniqueUsername.class);

        // Vérification des attributs de l'annotation
        assertEquals("Nom d'utilisateur déjà utilisé", annotation.message()); // Vérification du message par défaut
        assertArrayEquals(new Class<?>[]{}, annotation.groups()); // Vérification des groupes par défaut
        assertArrayEquals(new Class<?>[]{}, annotation.payload()); // Vérification des payloads par défaut

        // Vérification des métadonnées de l'annotation
        Target target = UniqueUsername.class.getAnnotation(Target.class);
        assertEquals(1, target.value().length); // Vérification qu'il y a une seule cible
        assertEquals(ElementType.FIELD, target.value()[0]); // Vérification que la cible est un champ

        Retention retention = UniqueUsername.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value()); // Vérification de la politique de rétention

        Constraint constraint = UniqueUsername.class.getAnnotation(Constraint.class);
        assertEquals(1, constraint.validatedBy().length); // Vérification qu'il y a un seul validateur
        assertEquals(UniqueUsernameValidator.class, constraint.validatedBy()[0]); // Vérification du validateur utilisé
    }

    @Test
    void uniqueUsernameAnnotation_ShouldBeAnnotation() {
        // Vérification que UniqueUsername est une annotation
        assertTrue(UniqueUsername.class.isAnnotation());
    }

    @Test
    void uniqueUsernameAnnotation_ShouldHaveCorrectTarget() {
        // Vérification que l'annotation cible les champs
        Target target = UniqueUsername.class.getAnnotation(Target.class);
        assertEquals(ElementType.FIELD, target.value()[0]);
    }

    @Test
    void uniqueUsernameAnnotation_ShouldHaveCorrectRetention() {
        // Vérification que l'annotation est conservée au runtime
        Retention retention = UniqueUsername.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void uniqueUsernameAnnotation_ShouldHaveCorrectConstraint() {
        // Vérification que l'annotation utilise le validateur UniqueUsernameValidator
        Constraint constraint = UniqueUsername.class.getAnnotation(Constraint.class);
        assertEquals(UniqueUsernameValidator.class, constraint.validatedBy()[0]);
    }
}