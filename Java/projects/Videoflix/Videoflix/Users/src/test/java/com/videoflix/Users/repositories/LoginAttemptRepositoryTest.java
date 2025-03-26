package com.videoflix.Users.repositories;

import com.videoflix.users_microservice.entities.LoginAttempt;
import com.videoflix.users_microservice.repositories.LoginAttemptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LoginAttemptRepositoryTest {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private TestEntityManager entityManager;

    private LoginAttempt loginAttempt1;
    private LoginAttempt loginAttempt2;
    private LoginAttempt loginAttempt3;

    @BeforeEach
    void setUp() {
        // Création de données de test avant chaque méthode de test
        LocalDateTime now = LocalDateTime.now();

        loginAttempt1 = new LoginAttempt();
        loginAttempt1.setUsername("utilisateur1");
        loginAttempt1.setAttemptTime(now.minusMinutes(10));
        entityManager.persist(loginAttempt1);

        loginAttempt2 = new LoginAttempt();
        loginAttempt2.setUsername("utilisateur1");
        loginAttempt2.setAttemptTime(now.minusMinutes(5));
        entityManager.persist(loginAttempt2);

        loginAttempt3 = new LoginAttempt();
        loginAttempt3.setUsername("utilisateur2");
        loginAttempt3.setAttemptTime(now.minusMinutes(15));
        entityManager.persist(loginAttempt3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Recherche des tentatives de connexion récentes pour un utilisateur")
    void testFindByUsernameAndAttemptTimeAfter() {
        // Préparation : définir le temps de référence pour la recherche
        LocalDateTime referenceTime = LocalDateTime.now().minusMinutes(7);

        // Exécution : rechercher les tentatives de connexion récentes
        List<LoginAttempt> tentativesRecentes = loginAttemptRepository.findByUsernameAndAttemptTimeAfter("utilisateur1",
                referenceTime);

        // Vérification : s'assurer que seules les tentatives récentes sont retournées
        assertThat(tentativesRecentes)
                .hasSize(1) // Une seule tentative après le temps de référence
                .containsExactly(loginAttempt2); // La tentative la plus récente
    }

    @Test
    @DisplayName("Recherche de tentatives de connexion pour un utilisateur sans correspondance")
    void testFindByUsernameAndAttemptTimeAfterNoResults() {
        // Préparation : définir un temps de référence très récent
        LocalDateTime referenceTime = LocalDateTime.now().minusSeconds(1);

        // Exécution : rechercher les tentatives de connexion
        List<LoginAttempt> tentativesRecentes = loginAttemptRepository.findByUsernameAndAttemptTimeAfter("utilisateur1",
                referenceTime);

        // Vérification : aucune tentative ne devrait être trouvée
        assertThat(tentativesRecentes).isEmpty();
    }
}