package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.Level;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class SubscriptionLevelRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private SubscriptionLevelRepository subscriptionLevelRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité SubscriptionLevel pour
    // les tests.
    private SubscriptionLevel createAndPersistSubscriptionLevel(Level level, String description, double price) {
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
        subscriptionLevel.setLevel(level);
        subscriptionLevel.setDescription(description);
        subscriptionLevel.setPrice(price);
        return entityManager.persistAndFlush(subscriptionLevel);
    }

    // Test pour vérifier la méthode findByLevel lorsque le niveau d'abonnement
    // existe.
    @Test
    void findByLevel_shouldReturnSubscriptionLevelForGivenLevelEnum() {
        // GIVEN : Création et persistence d'un niveau d'abonnement avec un Level
        // spécifique.
        Level levelEnum = Level.PREMIUM;
        String description = "Accès à tout le contenu en HD.";
        double price = 19.99;
        SubscriptionLevel persistedLevel = createAndPersistSubscriptionLevel(levelEnum, description, price);

        // WHEN : Recherche du niveau d'abonnement par son enum Level.
        Optional<SubscriptionLevel> foundLevel = subscriptionLevelRepository.findByLevel(levelEnum);

        // THEN : Vérification qu'un Optional contenant le niveau d'abonnement attendu
        // est retourné.
        assertTrue(foundLevel.isPresent());
        assertEquals(persistedLevel.getId(), foundLevel.get().getId());
        assertEquals(levelEnum, foundLevel.get().getLevel());
        assertEquals(description, foundLevel.get().getDescription());
        assertEquals(price, foundLevel.get().getPrice());
    }

    // Test pour vérifier la méthode findByLevel lorsqu'aucun niveau d'abonnement ne
    // correspond à l'enum Level donné.
    @Test
    void findByLevel_shouldReturnEmptyOptionalIfLevelNotFound() {
        // GIVEN : Création et persistence d'un niveau d'abonnement avec un Level
        // spécifique.
        createAndPersistSubscriptionLevel(Level.BASIC, "Accès au contenu standard.", 9.99);

        // WHEN : Recherche d'un niveau d'abonnement par un enum Level qui n'existe pas
        // dans la base de données.
        Optional<SubscriptionLevel> foundLevel = subscriptionLevelRepository.findByLevel(Level.ULTRA);

        // THEN : Vérification qu'un Optional vide est retourné.
        assertTrue(foundLevel.isEmpty());
    }
}