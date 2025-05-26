package com.videoflix.subscriptions_microservice.validations;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires optimisés pour la classe SubscriptionLevelValidator.
 * Utilise les tests paramétrés de JUnit 5 pour tester efficacement
 * différents scénarios de validation.
 */
@DisplayName("Tests du validateur de niveau d'abonnement (SubscriptionLevelValidator)")
class SubscriptionLevelValidatorTest {

    // Le validateur à tester
    private SubscriptionLevelValidator validator;

    // Un contexte de validation simulé (mock), car nous n'avons pas besoin de
    // tester
    // ses interactions complexes pour ce validateur simple.
    private ConstraintValidatorContext context;

    /**
     * Méthode d'initialisation exécutée avant chaque test.
     * Elle crée une nouvelle instance du validateur, l'initialise
     * (même si l'annotation est null ici, car notre `initialize` ne l'utilise pas)
     * et crée un mock pour le contexte.
     */
    @BeforeEach
    void setUp() {
        validator = new SubscriptionLevelValidator();
        // Initialise le validateur avec les types autorisés ("Basic", "Premium",
        // "Ultra").
        // On passe 'null' car la méthode initialize actuelle n'utilise pas
        // l'annotation.
        validator.initialize(null);
        // Crée un mock pour ConstraintValidatorContext
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    /**
     * Test paramétré pour vérifier que les niveaux d'abonnement valides
     * et la valeur 'null' passent la validation.
     *
     * @param validLevel Le niveau d'abonnement à tester (y compris null).
     */
    @ParameterizedTest
    @ValueSource(strings = { "Basic", "Premium", "Ultra" }) // Fournit les chaînes valides
    @NullSource // Fournit également une valeur null
    @DisplayName("Doit retourner VRAI pour les niveaux valides et null")
    void testValidLevelsIncludingNullReturnTrue(String validLevel) {
        // Assertion : Vérifie que la méthode isValid retourne true
        assertTrue(validator.isValid(validLevel, context),
                "Le niveau '" + validLevel + "' devrait être considéré comme valide.");
    }

    /**
     * Test paramétré pour vérifier que les niveaux d'abonnement invalides
     * (mauvaise casse, valeur inexistante, chaîne vide) échouent à la validation.
     *
     * @param invalidLevel Le niveau d'abonnement invalide à tester.
     */
    @ParameterizedTest
    @ValueSource(strings = { "basic", "premium", "Free", "Standard", " " }) // Fournit des chaînes invalides
    @EmptySource // Fournit également une chaîne vide ("")
    @DisplayName("Doit retourner FAUX pour les niveaux invalides ou vides")
    void testInvalidLevelsReturnFalse(String invalidLevel) {
        // Assertion : Vérifie que la méthode isValid retourne false
        assertFalse(validator.isValid(invalidLevel, context),
                "Le niveau '" + invalidLevel + "' devrait être considéré comme invalide.");
    }

    /**
     * Test spécifique pour s'assurer que l'initialisation a bien chargé les types.
     * Bien que couvert par les autres tests, cela peut être utile pour la clarté.
     * Note: Ce test est un peu redondant avec les précédents mais explicite
     * que l'initialisation est bien prise en compte.
     */
    @Test
    @DisplayName("Vérifie que 'Basic' est valide après initialisation")
    void testInitializationWorks() {
        // Assertion : Vérifie qu'un type connu est valide après l'appel à initialize()
        assertTrue(validator.isValid("Basic", context),
                "'Basic' devrait être valide si l'initialisation a fonctionné.");
    }
}