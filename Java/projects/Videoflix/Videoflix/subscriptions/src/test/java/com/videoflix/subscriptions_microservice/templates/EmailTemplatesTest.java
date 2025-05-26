package com.videoflix.subscriptions_microservice.templates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe EmailTemplates.
 * Cette classe teste tous les templates d'email et leurs formatages.
 */
@DisplayName("Tests des templates d'emails Videoflix")
class EmailTemplatesTest {

    // Données de test communes
    private static final String TEST_USER_NAME = "John Doe";
    private static final String TEST_SUBSCRIPTION_TYPE = "Premium";
    private static final String TEST_DATE = "15/12/2024";
    private static final int TEST_DAYS = 5;
    private static final String EXPECTED_SIGNATURE = "L'équipe Videoflix";

    // Regex pour compter les placeholders de String.format (ex: %s, %d, mais pas
    // %%)
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%(?![%n])[scdfx]");

    /**
     * Vérifie que la classe EmailTemplates ne peut pas être instanciée.
     * La classe étant utilitaire, elle doit avoir un constructeur privé.
     */
    @Test
    @DisplayName("Vérification de l'impossibilité d'instancier EmailTemplates")
    void testClassCannotBeInstantiated() {
        assertThrows(InvocationTargetException.class, () -> {
            Constructor<EmailTemplates> constructor = EmailTemplates.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }, "La classe EmailTemplates ne devrait pas être instanciable.");
    }

    /**
     * Fournit les sujets d'emails pour les tests paramétrés.
     */
    private static Stream<Arguments> emailSubjectsProvider() {
        return Stream.of(
                Arguments.of(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRING, "d'expiration d'abonnement"),
                Arguments.of(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRED, "d'abonnement expiré"),
                Arguments.of(EmailTemplates.SUBJECT_TRIAL_ENDING, "de fin d'essai"),
                Arguments.of(EmailTemplates.SUBJECT_TRIAL_ENDED, "d'essai terminé"),
                Arguments.of(EmailTemplates.SUBJECT_WELCOME, "de bienvenue"),
                Arguments.of(EmailTemplates.SUBJECT_PAYMENT_REMINDER, "de rappel de paiement"));
    }

    /**
     * Test paramétré pour vérifier que tous les sujets d'emails sont définis et non
     * vides.
     */
    @ParameterizedTest
    @MethodSource("emailSubjectsProvider")
    @DisplayName("Vérification des sujets d'emails (non nuls et non vides)")
    void testEmailSubjects(String subject, String description) {
        assertAll("Sujet " + description,
                () -> assertNotNull(subject, "Le sujet " + description + " ne doit pas être null"),
                () -> assertFalse(subject.trim().isEmpty(), "Le sujet " + description + " ne doit pas être vide"));
    }

    /**
     * Test du template d'email de notification d'abonnement.
     */
    @Test
    @DisplayName("Test du template de notification d'abonnement")
    void testSubscriptionNotificationTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.SUBSCRIPTION_NOTIFICATION,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE,
                TEST_DATE,
                TEST_DAYS);

        assertAll("Contenu de l'email de notification d'abonnement",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains(TEST_DATE), "Doit contenir la date"),
                () -> assertTrue(formattedEmail.contains(String.valueOf(TEST_DAYS)), "Doit contenir les jours"),
                () -> assertTrue(formattedEmail.contains("Videoflix"), "Doit contenir la marque"),
                () -> assertTrue(formattedEmail.contains("facturation"), "Doit mentionner la facturation"));
    }

    /**
     * Test du template d'email de bienvenue.
     */
    @Test
    @DisplayName("Test du template d'email de bienvenue")
    void testWelcomeEmailTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.WELCOME_EMAIL,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE);

        assertAll("Contenu de l'email de bienvenue",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains("Bienvenue"), "Doit contenir 'Bienvenue'"),
                () -> assertTrue(formattedEmail.contains("bibliothèque"), "Doit mentionner la bibliothèque"),
                () -> assertTrue(formattedEmail.contains("compte"), "Doit mentionner le compte"),
                () -> assertTrue(formattedEmail.contains("catalogue"), "Doit mentionner le catalogue"));
    }

    /**
     * Test du template d'email de fin de période d'essai.
     */
    @Test
    @DisplayName("Test du template de fin de période d'essai")
    void testTrialPeriodEndingTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.TRIAL_PERIOD_ENDING_NOTIFICATION,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE,
                TEST_DATE);

        assertAll("Contenu de l'email de fin d'essai",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains(TEST_DATE), "Doit contenir la date"),
                () -> assertTrue(formattedEmail.contains("période d'essai"), "Doit mentionner la période d'essai"),
                () -> assertTrue(formattedEmail.contains("plan payant"), "Doit mentionner les plans payants"));
    }

    /**
     * Test du template d'email d'abonnement qui expire.
     */
    @Test
    @DisplayName("Test du template d'abonnement qui expire")
    void testSubscriptionExpiringTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.SUBSCRIPTION_EXPIRING_EMAIL,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE,
                TEST_DATE);

        assertAll("Contenu de l'email d'expiration",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains(TEST_DATE), "Doit contenir la date"),
                () -> assertTrue(formattedEmail.contains("expire"), "Doit mentionner l'expiration"),
                () -> assertTrue(formattedEmail.contains("renouveler"), "Doit mentionner le renouvellement"));
    }

    /**
     * Test du template d'email d'abonnement expiré.
     */
    @Test
    @DisplayName("Test du template d'abonnement expiré")
    void testSubscriptionExpiredTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.SUBSCRIPTION_EXPIRED_EMAIL,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE,
                TEST_DATE);

        assertAll("Contenu de l'email d'abonnement expiré",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains(TEST_DATE), "Doit contenir la date"),
                () -> assertTrue(formattedEmail.contains("expiré"), "Doit mentionner que l'abonnement a expiré"),
                () -> assertTrue(formattedEmail.contains("retrouver l'accès"),
                        "Doit mentionner la récupération d'accès"));
    }

    /**
     * Test du template d'email d'essai terminé.
     */
    @Test
    @DisplayName("Test du template d'essai terminé")
    void testTrialEndedTemplate() {
        String formattedEmail = String.format(
                EmailTemplates.TRIAL_ENDED_EMAIL,
                TEST_USER_NAME,
                TEST_SUBSCRIPTION_TYPE,
                TEST_DATE);

        assertAll("Contenu de l'email d'essai terminé",
                () -> assertTrue(formattedEmail.contains(TEST_USER_NAME), "Doit contenir le nom"),
                () -> assertTrue(formattedEmail.contains(TEST_SUBSCRIPTION_TYPE), "Doit contenir le type"),
                () -> assertTrue(formattedEmail.contains(TEST_DATE), "Doit contenir la date"),
                () -> assertTrue(formattedEmail.contains("terminée"), "Doit mentionner que l'essai est terminé"),
                () -> assertTrue(formattedEmail.contains("plan payant"), "Doit mentionner les plans payants"));
    }

    /**
     * Fournit tous les templates d'emails pour les tests paramétrés.
     */
    private static Stream<String> emailTemplatesProvider() {
        return Stream.of(
                EmailTemplates.SUBSCRIPTION_NOTIFICATION,
                EmailTemplates.WELCOME_EMAIL,
                EmailTemplates.TRIAL_PERIOD_ENDING_NOTIFICATION,
                EmailTemplates.SUBSCRIPTION_EXPIRING_EMAIL,
                EmailTemplates.SUBSCRIPTION_EXPIRED_EMAIL,
                EmailTemplates.TRIAL_ENDED_EMAIL);
    }

    /**
     * Test paramétré pour vérifier la cohérence de la signature dans tous les
     * templates.
     */
    @ParameterizedTest
    @MethodSource("emailTemplatesProvider")
    @DisplayName("Vérification de la signature dans tous les templates")
    void testEmailSignatureConsistency(String template) {
        assertTrue(template.contains(EXPECTED_SIGNATURE),
                "Chaque template doit contenir la signature : '" + EXPECTED_SIGNATURE + "'");
    }

    /**
     * Test paramétré pour vérifier que les templates ont une longueur raisonnable.
     */
    @ParameterizedTest
    @MethodSource("emailTemplatesProvider")
    @DisplayName("Vérification de la longueur des templates")
    void testEmailTemplatesLength(String template) {
        assertTrue(template.length() > 50, "Le template doit avoir une longueur minimale de 50 caractères.");
        assertTrue(template.length() < 2000, "Le template ne doit pas dépasser 2000 caractères.");
    }

    /**
     * Test avec des paramètres vides ou null.
     */
    @Test
    @DisplayName("Test avec des paramètres vides ou null")
    void testTemplatesWithEmptyOrNullParameters() {
        assertAll("Gestion des paramètres invalides",
                () -> {
                    String emailWithNullName = String.format(EmailTemplates.WELCOME_EMAIL, (String) null,
                            TEST_SUBSCRIPTION_TYPE);
                    assertTrue(emailWithNullName.contains("null"),
                            "Doit gérer les paramètres null en affichant 'null'.");
                },
                () -> {
                    String emailWithEmptyName = String.format(EmailTemplates.WELCOME_EMAIL, "", TEST_SUBSCRIPTION_TYPE);
                    assertNotNull(emailWithEmptyName, "Ne doit pas être null avec des paramètres vides.");
                    assertTrue(emailWithEmptyName.contains("Bonjour ,"), "Doit gérer les chaînes vides.");
                });
    }

    /**
     * Fournit les templates et le nombre attendu de placeholders.
     */
    private static Stream<Arguments> placeholderCountProvider() {
        return Stream.of(
                Arguments.of(EmailTemplates.SUBSCRIPTION_NOTIFICATION, 4),
                Arguments.of(EmailTemplates.WELCOME_EMAIL, 2),
                Arguments.of(EmailTemplates.TRIAL_PERIOD_ENDING_NOTIFICATION, 3),
                Arguments.of(EmailTemplates.SUBSCRIPTION_EXPIRING_EMAIL, 3),
                Arguments.of(EmailTemplates.SUBSCRIPTION_EXPIRED_EMAIL, 3),
                Arguments.of(EmailTemplates.TRIAL_ENDED_EMAIL, 3));
    }

    /**
     * Compte le nombre de placeholders dans une chaîne.
     */
    private long countPlaceholders(String template) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        return matcher.results().count();
    }

    /**
     * Test paramétré pour vérifier le nombre de placeholders dans chaque template.
     */
    @ParameterizedTest
    @MethodSource("placeholderCountProvider")
    @DisplayName("Vérification du nombre de placeholders dans les templates")
    void testPlaceholderCount(String template, int expectedCount) {
        assertEquals(expectedCount, countPlaceholders(template),
                "Le nombre de placeholders ne correspond pas pour le template.");
    }
}