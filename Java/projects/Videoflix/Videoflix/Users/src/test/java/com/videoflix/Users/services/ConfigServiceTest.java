package com.videoflix.Users.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.videoflix.users_microservice.services.ConfigService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de tests pour le service de configuration.
 * 
 * Ces tests vérifient le comportement du service de configuration,
 * en se concentrant sur la récupération des propriétés de configuration
 * de la base de données.
 */
class ConfigServiceTest {

    /**
     * Instance du service de configuration à tester
     */
    private ConfigService configService;

    /**
     * Données de test pour les configurations de base de données
     */
    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/videoflix";
    private static final String TEST_DB_USER = "videoflix_user";
    private static final String TEST_DB_PASSWORD = "secret_password";

    /**
     * Méthode de configuration exécutée avant chaque test.
     * 
     * Initialise le service de configuration et injecte les valeurs de test
     * en utilisant la réflexion pour simuler l'injection de valeurs par @Value.
     */
    @BeforeEach
    void setUp() {
        // Créer une nouvelle instance de ConfigService
        configService = new ConfigService();

        // Injection des valeurs de test en utilisant la réflexion
        ReflectionTestUtils.setField(configService, "databaseUrl", TEST_DB_URL);
        ReflectionTestUtils.setField(configService, "databaseUser", TEST_DB_USER);
        ReflectionTestUtils.setField(configService, "databasePassword", TEST_DB_PASSWORD);
    }

    /**
     * Test de récupération de l'URL de la base de données.
     * 
     * Objectifs :
     * - Vérifier que l'URL de la base de données est correctement récupérée
     * - Confirmer que la valeur correspond à celle injectée
     */
    @Test
    void getDatabaseUrl_ShouldReturnCorrectUrl() {
        // Exécution : Récupérer l'URL de la base de données
        String retrievedUrl = configService.getDatabaseUrl();

        // Vérification : Confirmer que l'URL correspond à la valeur de test
        assertEquals(TEST_DB_URL, retrievedUrl,
                "L'URL de la base de données doit correspondre à la valeur injectée");
    }

    /**
     * Test de récupération du nom d'utilisateur de la base de données.
     * 
     * Objectifs :
     * - Vérifier que le nom d'utilisateur est correctement récupéré
     * - Confirmer que la valeur correspond à celle injectée
     */
    @Test
    void getDatabaseUser_ShouldReturnCorrectUser() {
        // Exécution : Récupérer le nom d'utilisateur de la base de données
        String retrievedUser = configService.getDatabaseUser();

        // Vérification : Confirmer que le nom d'utilisateur correspond à la valeur de
        // test
        assertEquals(TEST_DB_USER, retrievedUser,
                "Le nom d'utilisateur de la base de données doit correspondre à la valeur injectée");
    }

    /**
     * Test de récupération du mot de passe de la base de données.
     * 
     * Objectifs :
     * - Vérifier que le mot de passe est correctement récupéré
     * - Confirmer que la valeur correspond à celle injectée
     */
    @Test
    void getDatabasePassword_ShouldReturnCorrectPassword() {
        // Exécution : Récupérer le mot de passe de la base de données
        String retrievedPassword = configService.getDatabasePassword();

        // Vérification : Confirmer que le mot de passe correspond à la valeur de test
        assertEquals(TEST_DB_PASSWORD, retrievedPassword,
                "Le mot de passe de la base de données doit correspondre à la valeur injectée");
    }

    /**
     * Test vérifiant que les configurations sont distinctes.
     * 
     * Objectifs :
     * - Confirmer que chaque configuration est unique
     * - Garantir qu'il n'y a pas de confusion entre les valeurs
     */
    @Test
    void configurations_ShouldBeDifferent() {
        // Vérification : Les différentes configurations doivent être distinctes
        assertAll(
                () -> assertNotEquals(configService.getDatabaseUrl(), configService.getDatabaseUser(),
                        "L'URL et le nom d'utilisateur ne doivent pas être identiques"),
                () -> assertNotEquals(configService.getDatabaseUrl(), configService.getDatabasePassword(),
                        "L'URL et le mot de passe ne doivent pas être identiques"),
                () -> assertNotEquals(configService.getDatabaseUser(), configService.getDatabasePassword(),
                        "Le nom d'utilisateur et le mot de passe ne doivent pas être identiques"));
    }

    /**
     * Test de non-nullité des configurations.
     * 
     * Objectifs :
     * - Vérifier que toutes les configurations sont non nulles
     * - S'assurer qu'aucune valeur de configuration n'est manquante
     */
    @Test
    void configurations_ShouldNotBeNull() {
        // Vérification : Toutes les configurations doivent être non nulles
        assertAll(
                () -> assertNotNull(configService.getDatabaseUrl(),
                        "L'URL de la base de données ne doit pas être nulle"),
                () -> assertNotNull(configService.getDatabaseUser(),
                        "Le nom d'utilisateur de la base de données ne doit pas être nul"),
                () -> assertNotNull(configService.getDatabasePassword(),
                        "Le mot de passe de la base de données ne doit pas être nul"));
    }
}