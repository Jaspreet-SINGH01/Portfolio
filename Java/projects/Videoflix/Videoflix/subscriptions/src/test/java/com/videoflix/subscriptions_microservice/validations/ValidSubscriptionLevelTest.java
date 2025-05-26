package com.videoflix.subscriptions_microservice.validations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration pour l'annotation @ValidSubscriptionLevel.
 * Vérifie que l'annotation déclenche correctement la validation
 * lorsqu'elle est utilisée sur un bean.
 */
@DisplayName("Tests d'intégration de l'annotation @ValidSubscriptionLevel")
class ValidSubscriptionLevelAnnotationTest {

    // Fabrique de validateurs, initialisée une seule fois pour tous les tests.
    private static ValidatorFactory validatorFactory;
    // Validateur, initialisé une seule fois.
    private static Validator validator;

    /**
     * Classe interne simple utilisée comme 'bean' pour les tests.
     * Elle contient un champ annoté avec @ValidSubscriptionLevel.
     */
    private static class TestBean {
        @ValidSubscriptionLevel
        private String subscriptionLevel;

        // Constructeur
        public TestBean(String subscriptionLevel) {
            this.subscriptionLevel = subscriptionLevel;
        }
    }

    /**
     * Met en place la fabrique de validateurs et le validateur avant tous les
     * tests.
     * C'est plus efficace que de le faire avant chaque test.
     */
    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
        System.out.println("ValidatorFactory et Validator initialisés.");
    }

    /**
     * Nettoie la fabrique de validateurs après tous les tests.
     */
    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
            System.out.println("ValidatorFactory fermée.");
        }
    }

    /**
     * Test paramétré pour les niveaux valides et null.
     * Vérifie qu'aucune violation de contrainte n'est générée.
     * 
     * @param level Le niveau à tester ("Basic", "Premium", "Ultra", ou null).
     */
    @ParameterizedTest
    @ValueSource(strings = { "Basic", "Premium", "Ultra" }) // Niveaux valides
    @NullSource // Valeur null (qui est considérée comme valide par notre validateur)
    @DisplayName("Doit être valide pour les niveaux corrects et null")
    void whenLevelIsValidOrNull_thenNoViolations(String level) {
        // Crée un bean avec le niveau à tester.
        TestBean bean = new TestBean(level);
        // Valide le bean.
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        // Assertion : Aucune violation ne doit être trouvée.
        assertTrue(violations.isEmpty(),
                "Le niveau '" + level + "' devrait être valide, aucune violation attendue.");
    }

    /**
     * Test pour un niveau invalide.
     * Vérifie qu'une seule violation est générée et que le message d'erreur
     * est celui par défaut de l'annotation.
     */
    @Test
    @DisplayName("Doit être invalide pour un niveau incorrect")
    void whenLevelIsInvalid_thenOneViolation() {
        // Crée un bean avec un niveau invalide.
        TestBean bean = new TestBean("FreeTrial");
        // Valide le bean.
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        // Assertions :
        assertAll("Vérifications pour niveau invalide",
                // 1. Une seule violation doit être trouvée.
                () -> assertEquals(1, violations.size(),
                        "Une seule violation était attendue pour 'FreeTrial'."),
                // 2. Le message doit être le message par défaut.
                () -> assertEquals("Le type d'abonnement n'est pas valide",
                        violations.iterator().next().getMessage(),
                        "Le message d'erreur ne correspond pas au message par défaut."));
    }

    /**
     * Test pour un niveau invalide (chaîne vide).
     * Vérifie qu'une seule violation est générée.
     */
    @Test
    @DisplayName("Doit être invalide pour une chaîne vide")
    void whenLevelIsEmpty_thenOneViolation() {
        // Crée un bean avec une chaîne vide.
        TestBean bean = new TestBean("");
        // Valide le bean.
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        // Assertion : Une seule violation doit être trouvée.
        assertEquals(1, violations.size(),
                "Une violation était attendue pour une chaîne vide.");
    }
}