package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Payment;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class PaymentRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private PaymentRepository paymentRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité Payment pour les tests.
    private Payment createAndPersistPayment(Long subscriptionId, String paymentMethod, double amount) {
        Payment payment = new Payment();
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        payment.setSubscription(subscription);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        return entityManager.persistAndFlush(payment);
    }

    // Test pour vérifier la méthode findBySubscriptionId du repository.
    @Test
    void findBySubscriptionId_shouldReturnPaymentsForGivenSubscriptionId() {
        // GIVEN : Création et persistence de plusieurs paiements associés à différents
        // IDs d'abonnement.
        createAndPersistPayment(1L, "credit_card", 29.99);

        // WHEN : Recherche des paiements par un ID d'abonnement spécifique (1L).
        List<Payment> payments = paymentRepository.findBySubscriptionId(1L);

        // THEN : Vérification que la liste retournée contient les deux paiements
        // associés à l'abonnement 1L.
        assertEquals(2, payments.size());
        assertTrue(payments.stream().anyMatch(payment -> payment.getPaymentMethod().equals("credit_card")));
        assertTrue(payments.stream().anyMatch(payment -> payment.getPaymentMethod().equals("bank_transfer")));
        assertTrue(payments.stream().allMatch(payment -> payment.getSubscription().getId().equals(1L)));
    }

    // Test pour vérifier que la méthode findBySubscriptionId retourne une liste
    // vide si aucun paiement n'est trouvé pour l'ID d'abonnement donné.
    @Test
    void findBySubscriptionId_shouldReturnEmptyListIfNoPaymentsForSubscription() {
        // GIVEN : Création et persistence d'un paiement pour un ID d'abonnement (2L).
        createAndPersistPayment(2L, "paypal", 19.99);

        // WHEN : Recherche des paiements pour un ID d'abonnement différent (3L) pour
        // lequel aucun paiement n'existe.
        List<Payment> payments = paymentRepository.findBySubscriptionId(3L);

        // THEN : Vérification que la liste retournée est vide.
        assertTrue(payments.isEmpty());
    }
}