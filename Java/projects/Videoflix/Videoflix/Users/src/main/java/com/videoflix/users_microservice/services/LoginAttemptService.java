package com.videoflix.users_microservice.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.videoflix.users_microservice.entities.LoginAttempt;
import com.videoflix.users_microservice.repositories.LoginAttemptRepository;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5; // Nombre maximal de tentatives de connexion autorisées.
    private static final int BLOCKING_TIME = 5; // Durée de blocage en minutes.
    private final LoginAttemptRepository loginAttemptRepository;

    /**
     * Constructeur pour LoginAttemptService.
     * Injecte le LoginAttemptRepository pour accéder à la base de données des
     * tentatives de connexion.
     *
     * @param loginAttemptRepository Le repository pour les tentatives de connexion.
     */
    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    /**
     * Enregistre une tentative de connexion échouée pour un utilisateur donné.
     *
     * @param username Le nom d'utilisateur pour lequel enregistrer la tentative de
     *                 connexion échouée.
     */
    @Transactional
    public void recordFailedLogin(String username) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }

    /**
     * Réinitialise les tentatives de connexion pour un utilisateur donné.
     * Supprime toutes les tentatives de connexion survenues après le temps de
     * blocage.
     *
     * @param username Le nom d'utilisateur pour lequel réinitialiser les tentatives
     *                 de connexion.
     */
    @Transactional
    public void resetLoginAttempts(String username) {
        LocalDateTime blockingTime = LocalDateTime.now().minusMinutes(BLOCKING_TIME);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndAttemptTimeAfter(username, blockingTime);
        loginAttemptRepository.deleteAll(attempts);
    }

    /**
     * Vérifie si un utilisateur est bloqué en fonction du nombre de tentatives de
     * connexion échouées.
     *
     * @param username Le nom d'utilisateur à vérifier.
     * @return true si l'utilisateur est bloqué, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isBlocked(String username) {
        LocalDateTime blockingTime = LocalDateTime.now().minusMinutes(BLOCKING_TIME);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndAttemptTimeAfter(username, blockingTime);
        return attempts.size() >= MAX_ATTEMPTS;
    }

    /**
     * Obtient le nombre de tentatives de connexion échouées pour un utilisateur
     * donné.
     *
     * @param username Le nom d'utilisateur pour lequel obtenir le nombre de
     *                 tentatives de connexion échouées.
     * @return Le nombre de tentatives de connexion échouées.
     */
    @Transactional(readOnly = true)
    public int getFailedAttempts(String username) {
        LocalDateTime blockingTime = LocalDateTime.now().minusMinutes(BLOCKING_TIME);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndAttemptTimeAfter(username, blockingTime);
        return attempts.size();
    }

    /**
     * Bloque un utilisateur.
     * Le blocage est géré implicitement par isBlocked().
     * Cette méthode est gardée pour la compatibilité avec le code existant.
     *
     * @param username Le nom d'utilisateur à bloquer.
     */
    @Transactional
    public void blockUser(String username) {
        // Le blocage est géré implicitement par isBlocked()
        // Cette méthode est gardée pour la compatibilité avec le code existant
    }
}