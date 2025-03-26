package com.videoflix.Users.services;

import com.videoflix.users_microservice.entities.LoginAttempt;
import com.videoflix.users_microservice.repositories.LoginAttemptRepository;
import com.videoflix.users_microservice.services.LoginAttemptService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour le service de gestion des tentatives de connexion.
 * Utilise Mockito pour mocker les dépendances et JUnit 5 pour les tests.
 * 
 * Cette classe teste les mécanismes de suivi et de blocage des tentatives de
 * connexion
 * pour améliorer la sécurité de l'application.
 */
@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    // Mock du repository pour éviter les interactions réelles avec la base de
    // données
    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    // Injection du repository mocké dans le service testé
    @InjectMocks
    private LoginAttemptService loginAttemptService;

    // Nom d'utilisateur constant utilisé pour les tests
    private static final String TEST_USERNAME = "testuser";

    // Nombre maximum de tentatives de connexion avant blocage
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    // Stocke le moment actuel pour les scénarios de test basés sur le temps
    private LocalDateTime now;

    /**
     * Méthode de configuration exécutée avant chaque test.
     * Initialise le temps courant pour assurer la cohérence des tests.
     */
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    /**
     * Test vérifiant l'enregistrement d'une tentative de connexion échouée.
     * 
     * Objectifs du test :
     * 1. Vérifier que la méthode save du repository est appelée exactement une fois
     * 2. Confirmer que la tentative de connexion enregistrée a le bon nom
     * d'utilisateur
     * 3. Garantir que le moment de la tentative n'est pas null
     */
    @Test
    void recordFailedLogin_ShouldSaveLoginAttempt() {
        // Préparation et exécution
        loginAttemptService.recordFailedLogin(TEST_USERNAME);

        // Vérification : S'assurer que la méthode save est appelée avec les bons
        // paramètres
        verify(loginAttemptRepository, times(1)).save(argThat(attempt -> attempt.getUsername().equals(TEST_USERNAME) &&
                attempt.getAttemptTime() != null));
    }

    /**
     * Test vérifiant la réinitialisation des tentatives de connexion.
     * 
     * Objectifs du test :
     * 1. Récupérer les tentatives de connexion récentes
     * 2. Confirmer que toutes les tentatives sont supprimées
     */
    @Test
    void resetLoginAttempts_ShouldDeleteRecentAttempts() {
        // Préparation : Création d'une liste de tentatives de connexion
        List<LoginAttempt> attempts = createMultipleLoginAttempts(2);

        // Simulation du repository pour retourner ces tentatives
        when(loginAttemptRepository.findByUsernameAndAttemptTimeAfter(
                eq(TEST_USERNAME),
                any(LocalDateTime.class))).thenReturn(attempts);

        // Exécution : Réinitialisation des tentatives de connexion
        loginAttemptService.resetLoginAttempts(TEST_USERNAME);

        // Vérification : Confirmer que toutes les tentatives sont supprimées
        verify(loginAttemptRepository, times(1)).deleteAll(attempts);
    }

    /**
     * Test vérifiant le blocage d'un utilisateur après avoir dépassé
     * le nombre maximum de tentatives de connexion.
     */
    @Test
    void isBlocked_ShouldReturnTrueWhenExceedingMaxAttempts() {
        // Préparation : Création du nombre maximal de tentatives de connexion
        List<LoginAttempt> attempts = createMultipleLoginAttempts(MAX_LOGIN_ATTEMPTS);

        // Simulation du repository pour retourner ces tentatives
        when(loginAttemptRepository.findByUsernameAndAttemptTimeAfter(
                eq(TEST_USERNAME),
                any(LocalDateTime.class))).thenReturn(attempts);

        // Vérification : Confirmer que l'utilisateur est bloqué
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    /**
     * Test vérifiant qu'un utilisateur n'est pas bloqué
     * lorsqu'il est en dessous du nombre maximum de tentatives.
     */
    @Test
    void isBlocked_ShouldReturnFalseWhenBelowMaxAttempts() {
        // Préparation : Création de moins de tentatives que le maximum
        List<LoginAttempt> attempts = createMultipleLoginAttempts(MAX_LOGIN_ATTEMPTS - 1);

        // Simulation du repository pour retourner ces tentatives
        when(loginAttemptRepository.findByUsernameAndAttemptTimeAfter(
                eq(TEST_USERNAME),
                any(LocalDateTime.class))).thenReturn(attempts);

        // Vérification : Confirmer que l'utilisateur n'est pas bloqué
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    /**
     * Test vérifiant le décompte correct des tentatives de connexion échouées.
     */
    @Test
    void getFailedAttempts_ShouldReturnCorrectCount() {
        // Préparation : Création de 3 tentatives de connexion
        List<LoginAttempt> attempts = createMultipleLoginAttempts(3);

        // Simulation du repository pour retourner ces tentatives
        when(loginAttemptRepository.findByUsernameAndAttemptTimeAfter(
                eq(TEST_USERNAME),
                any(LocalDateTime.class))).thenReturn(attempts);

        // Vérification : Confirmer le nombre correct de tentatives
        assertEquals(3, loginAttemptService.getFailedAttempts(TEST_USERNAME));
    }

    /**
     * Test vérifiant que la méthode blockUser n'interagit pas avec le repository.
     * Suggère que le blocage peut être géré à un autre niveau de l'application.
     */
    @Test
    void blockUser_ShouldNotInteractWithRepository() {
        // Exécution : Appel de la méthode de blocage
        loginAttemptService.blockUser(TEST_USERNAME);

        // Vérification : S'assurer qu'aucune méthode du repository n'est appelée
        verify(loginAttemptRepository, never()).save(any(LoginAttempt.class));
        verify(loginAttemptRepository, never()).deleteAll(anyList());
        verify(loginAttemptRepository, never()).findByUsernameAndAttemptTimeAfter(
                anyString(),
                any(LocalDateTime.class));
    }

    /**
     * Méthode utilitaire pour créer plusieurs tentatives de connexion pour les
     * tests.
     * 
     * @param count Nombre de tentatives de connexion à créer
     * @return Liste de tentatives de connexion
     */
    private List<LoginAttempt> createMultipleLoginAttempts(int count) {
        List<LoginAttempt> attempts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            attempts.add(createLoginAttempt(TEST_USERNAME, now.minusMinutes(i)));
        }
        return attempts;
    }

    /**
     * Méthode utilitaire pour créer une unique tentative de connexion.
     * 
     * @param username    Nom d'utilisateur pour la tentative de connexion
     * @param attemptTime Moment de la tentative de connexion
     * @return Objet LoginAttempt
     */
    private LoginAttempt createLoginAttempt(String username, LocalDateTime attemptTime) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(attemptTime);
        return attempt;
    }
}