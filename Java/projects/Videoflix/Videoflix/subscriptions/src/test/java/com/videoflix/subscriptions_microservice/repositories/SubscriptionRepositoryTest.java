package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.Subscription.SubscriptionStatus;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class SubscriptionRepositoryTest {

        // @Autowired injecte l'instance du repository que nous voulons tester.
        @Autowired
        private SubscriptionRepository subscriptionRepository;

        // TestEntityManager est un utilitaire pour interagir avec la base de données de
        // test.
        @Autowired
        private TestEntityManager entityManager;

        // Méthode utilitaire pour créer et persister une entité Subscription pour les
        // tests.
        private Subscription createAndPersistSubscription(User user, SubscriptionStatus status, LocalDateTime startDate,
                        LocalDateTime endDate, LocalDateTime trialEndDate, LocalDateTime nextBillingDate,
                        LocalDateTime creationTimestamp, LocalDateTime lastPaymentDate, LocalDateTime lastActivity) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setStatus(status);
                subscription.setStartDate(startDate);
                subscription.setEndDate(endDate);
                subscription.setTrialEndDate(trialEndDate);
                subscription.setNextBillingDate(nextBillingDate);
                subscription.setCreationTimestamp(creationTimestamp);
                subscription.setPaymentDate(lastPaymentDate);
                subscription.setLastActivity(lastActivity);
                return entityManager.persistAndFlush(subscription);
        }

        // Méthode utilitaire pour créer et persister un utilisateur pour les tests.
        private User createAndPersistUser(Long id) {
                User user = new User();
                user.setId(id);
                return entityManager.persistAndFlush(user);
        }

        // Test pour vérifier la méthode findByUser.
        @Test
        void findByUser_shouldReturnSubscriptionsForGivenUser() {
                // GIVEN : Création et persistence de plusieurs abonnements pour différents
                // utilisateurs.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(30),
                                LocalDateTime.now(), null, LocalDateTime.now().plusMonths(1),
                                LocalDateTime.now().minusMonths(1),
                                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1));

                // WHEN : Recherche des abonnements pour user1.
                List<Subscription> user1Subscriptions = subscriptionRepository.findByUser(user1);

                // THEN : Vérification que la liste contient les abonnements attendus pour
                // user1.
                assertEquals(2, user1Subscriptions.size());
                assertTrue(user1Subscriptions.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE));
                assertTrue(user1Subscriptions.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED));
                assertFalse(user1Subscriptions.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.PENDING));
        }

        // Test pour vérifier la méthode countByStatus.
        @Test
        void countByStatus_shouldReturnCountOfSubscriptionsWithGivenStatus() {
                // GIVEN : Création et persistence de plusieurs abonnements avec différents
                // statuts.
                User user1 = createAndPersistUser(1L);
                User user2 = createAndPersistUser(2L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(30),
                                LocalDateTime.now(),
                                null, LocalDateTime.now().plusMonths(1), LocalDateTime.now(), null, null);
                createAndPersistSubscription(user2, SubscriptionStatus.PENDING, LocalDateTime.now().minusDays(15),
                                LocalDateTime.now().plusDays(15), null, LocalDateTime.now().plusMonths(2),
                                LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusMonths(2),
                                LocalDateTime.now().minusMonths(1), null, null, LocalDateTime.now(), null, null);

                // WHEN : Comptage des abonnements avec le statut ACTIVE.
                long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

                // THEN : Vérification que le compte correspond au nombre d'abonnements actifs.
                assertEquals(2, activeCount);
        }

        // Test pour vérifier la méthode countByStartDateBetween.
        @Test
        void countByStartDateBetween_shouldReturnCountOfSubscriptionsWithinGivenDateRange() {
                // GIVEN : Création et persistence d'abonnements avec différentes dates de
                // début.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(5).toLocalDate(),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.PENDING,
                                LocalDateTime.now().minusDays(1).toLocalDate(),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusDays(10).toLocalDate(), LocalDateTime.now(), null, null,
                                LocalDateTime.now(),
                                null, null);
                LocalDateTime startDate = LocalDateTime.now().minusDays(7);
                LocalDateTime endDate = LocalDateTime.now().minusDays(0);

                // WHEN : Comptage des abonnements dont la date de début est dans la plage.
                long count = subscriptionRepository.countByStartDateBetween(startDate, endDate);

                // THEN : Vérification que le compte correspond au nombre d'abonnements dans la
                // plage.
                assertEquals(2, count);
        }

        @SuppressWarnings("uncast")
        private void createAndPersistSubscription(User user, SubscriptionStatus cancelled, LocalDate localDate,
                        LocalDateTime now, Object trialEndDate, Object nextBillingDate, LocalDateTime now2,
                        Object lastPaymentDate,
                        Object lastActivity) {
                throw new UnsupportedOperationException("Unimplemented method 'createAndPersistSubscription'");
        }

        // Test pour vérifier la méthode
        // findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual.
        @Test
        void findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual_shouldReturnActiveSubscriptionsWithinGivenPeriod() {
                // GIVEN
                User user = createAndPersistUser(1L);
                LocalDateTime now = LocalDateTime.now();

                // Abonnement chevauchant la plage -> doit être trouvé
                createAndPersistSubscription(user, SubscriptionStatus.ACTIVE, now.minusDays(10), now.plusDays(10), now,
                                now, now, now, now);

                // Abonnement commençant après la plage -> ne doit PAS être trouvé
                createAndPersistSubscription(user, SubscriptionStatus.ACTIVE, now.plusDays(1), now.plusDays(15), now,
                                now, now, now, now);

                // Abonnement annulé dans la plage -> ne doit PAS être trouvé
                createAndPersistSubscription(user, SubscriptionStatus.CANCELLED, now.minusDays(5), now.minusDays(1),
                                now, now, now, now, now);

                LocalDateTime startDate = now.minusDays(5);
                LocalDateTime endDate = now.plusDays(5);

                // WHEN
                List<Subscription> result = subscriptionRepository
                                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                                SubscriptionStatus.ACTIVE, endDate, startDate);

                // THEN
                assertEquals(1, result.size());
                Subscription sub = result.get(0);
                assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
                assertTrue(sub.getStartDate().isBefore(startDate) || sub.getStartDate().isEqual(startDate));
                assertTrue(sub.getEndDate().isAfter(endDate) || sub.getEndDate().isEqual(endDate));
        }

        // Test pour vérifier la méthode countByStatusAndEndDateGreaterThanEqual.
        @Test
        void countByStatusAndEndDateGreaterThanEqual_shouldReturnCountOfActiveSubscriptionsEndingOnOrAfterGivenDate() {
                // GIVEN : Création d'abonnements actifs avec différentes dates de fin.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(30),
                                LocalDateTime.now().plusDays(5), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(15),
                                LocalDateTime.now().minusDays(1), null, null, LocalDateTime.now(), null, null);
                LocalDateTime endDateTime = LocalDateTime.now();

                // WHEN : Comptage des abonnements actifs dont la date de fin est supérieure ou
                // égale à la date donnée.
                long count = subscriptionRepository.countByStatusAndEndDateGreaterThanEqual(SubscriptionStatus.ACTIVE,
                                endDateTime);

                // THEN : Vérification que le compte correspond aux abonnements attendus.
                assertEquals(1, count);
        }

        // Test pour vérifier la méthode countByStatusAndStartDateLessThan.
        @Test
        void countByStatusAndStartDateLessThan_shouldReturnCountOfActiveSubscriptionsStartingBeforeGivenDate() {
                // GIVEN : Création d'abonnements actifs avec différentes dates de début.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now(),
                                null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().plusDays(1),
                                LocalDateTime.now(),
                                null, null, LocalDateTime.now(), null, null);
                LocalDateTime startDateTime = LocalDateTime.now();

                // WHEN : Comptage des abonnements actifs dont la date de début est antérieure à
                // la date donnée.
                long count = subscriptionRepository.countByStatusAndStartDateLessThan(SubscriptionStatus.ACTIVE,
                                startDateTime);

                // THEN : Vérification que le compte correspond aux abonnements attendus.
                assertEquals(1, count);
        }

        // Test pour vérifier la méthode findByEndDate.
        @Test
        void findByEndDate_shouldReturnSubscriptionsEndingOnGivenDate() {
                // GIVEN : Création d'abonnements avec différentes dates de fin.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now(),
                                null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.PENDING, LocalDateTime.now().minusDays(5),
                                LocalDateTime.now().plusDays(2), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED, LocalDateTime.now().minusMonths(1),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null, null);
                LocalDateTime today = LocalDateTime.now();

                // WHEN : Recherche des abonnements se terminant aujourd'hui.
                List<Subscription> endingToday = subscriptionRepository.findByEndDate(today);

                // THEN : Vérification que la liste contient les abonnements attendus.
                assertEquals(2, endingToday.size());
                assertTrue(endingToday.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE));
                assertTrue(endingToday.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED));
                assertFalse(endingToday.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.PENDING));
        }

        // Test pour vérifier la méthode findByEndDateBeforeAndStatusNot.
        @Test
        void findByEndDateBeforeAndStatusNot_shouldReturnNonExpiredSubscriptionsEndingBeforeGivenDate() {
                // GIVEN : Création d'abonnements avec différentes dates de fin et statuts.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now().minusDays(1), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.EXPIRED, LocalDateTime.now().minusDays(5),
                                LocalDateTime.now().minusDays(2), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED, LocalDateTime.now().minusMonths(1),
                                LocalDateTime.now().minusDays(3), null, null, LocalDateTime.now(), null, null);
                LocalDateTime today = LocalDateTime.now();
                SubscriptionStatus expiredStatus = SubscriptionStatus.EXPIRED;

                // WHEN : Recherche des abonnements non expirés se terminant avant aujourd'hui.
                List<Subscription> endingBeforeAndNotExpired = subscriptionRepository.findByEndDateBeforeAndStatusNot(
                                today,
                                expiredStatus);

                // THEN : Vérification que la liste contient les abonnements attendus.
                assertEquals(2, endingBeforeAndNotExpired.size());
                assertTrue(endingBeforeAndNotExpired.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE));
                assertTrue(endingBeforeAndNotExpired.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED));
                assertFalse(endingBeforeAndNotExpired.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.EXPIRED));
        }

        // Test pour vérifier la méthode findByTrialEndDate.
        @Test
        void findByTrialEndDate_shouldReturnSubscriptionsWithTrialEndingOnGivenDate() {
                // GIVEN : Création d'abonnements avec différentes dates de fin d'essai.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.TRIAL, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now().plusDays(7), LocalDateTime.now(), null, LocalDateTime.now(), null,
                                null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(5),
                                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(1), null,
                                LocalDateTime.now(), null, null);
                LocalDateTime today = LocalDateTime.now();

                // WHEN : Recherche des abonnements dont l'essai se termine aujourd'hui.
                List<Subscription> trialEndingToday = subscriptionRepository.findByTrialEndDate(today);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, trialEndingToday.size());
                assertTrue(trialEndingToday.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.TRIAL));
        }

        // Test pour vérifier la méthode findByTrialEndDateBeforeAndStatus.
        @Test
        void findByTrialEndDateBeforeAndStatus_shouldReturnTrialSubscriptionsEndingBeforeGivenDate() {
                // GIVEN : Création d'abonnements avec différentes dates de fin d'essai et
                // statuts.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.TRIAL, LocalDateTime.now().minusDays(15),
                                LocalDateTime.now().plusDays(5), LocalDateTime.now().minusDays(1), null,
                                LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now().plusDays(10), LocalDateTime.now(), null, LocalDateTime.now(), null,
                                null);
                LocalDateTime today = LocalDateTime.now();
                SubscriptionStatus trialStatus = SubscriptionStatus.TRIAL;

                // WHEN : Recherche des abonnements en essai se terminant avant aujourd'hui.
                List<Subscription> trialEndingBeforeToday = subscriptionRepository.findByTrialEndDateBeforeAndStatus(
                                today,
                                trialStatus);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, trialEndingBeforeToday.size());
                assertTrue(trialEndingBeforeToday.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.TRIAL
                                                && sub.getTrialEndDate().isBefore(today)));
        }

        // Test pour vérifier la méthode findByStatusAndCancellationDateBefore.
        @Test
        void findByStatusAndCancellationDateBefore_shouldReturnCancelledSubscriptionsCancelledBeforeGivenDate() {
                // GIVEN : Création d'abonnements annulés avec différentes dates d'annulation.
                User user1 = createAndPersistUser(1L);
                Subscription subscription1 = createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1), null, null,
                                LocalDateTime.now(), null, null);
                subscription1.setCancelledAt(LocalDateTime.now().minusDays(10));
                entityManager.persist(subscription1);
                Subscription subscription2 = createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(30), LocalDateTime.now(), null, null, LocalDateTime.now(),
                                null, null);
                subscription2.setCancelledAt(LocalDateTime.now().plusDays(5));
                entityManager.persist(subscription2);
                LocalDateTime archiveThreshold = LocalDateTime.now();
                SubscriptionStatus cancelledStatus = SubscriptionStatus.CANCELLED;

                // WHEN : Recherche des abonnements annulés avant la date de seuil d'archivage.
                List<Subscription> cancelledBeforeThreshold = subscriptionRepository
                                .findByStatusAndCancellationDateBefore(cancelledStatus, archiveThreshold);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, cancelledBeforeThreshold.size());
                assertTrue(cancelledBeforeThreshold.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED
                                                && sub.getCancelledAt().isBefore(archiveThreshold)));
        }

        // Test pour vérifier la méthode findInactiveBefore.
        @Test
        void findInactiveBefore_shouldReturnInactiveSubscriptionsBeforeGivenDate() {
                // GIVEN : Création d'abonnements avec différentes dates de dernière activité et
                // statuts.
                User user1 = createAndPersistUser(1L);
                Subscription subscription1 = createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2), null, null,
                                LocalDateTime.now(), null,
                                LocalDateTime.now().minusMonths(4));
                entityManager.persist(subscription1);
                Subscription subscription2 = createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(30), LocalDateTime.now(), null, null, LocalDateTime.now(),
                                null,
                                LocalDateTime.now().minusDays(2));
                entityManager.persist(subscription2);
                LocalDateTime deletionThreshold = LocalDateTime.now().minusMonths(3).plusDays(1);

                // WHEN : Recherche des abonnements inactifs avant la date de seuil de
                // suppression.
                List<Subscription> inactiveBeforeThreshold = subscriptionRepository
                                .findInactiveBefore(deletionThreshold);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, inactiveBeforeThreshold.size());
                assertTrue(inactiveBeforeThreshold.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED
                                                && sub.getLastActivity().isBefore(deletionThreshold)));
                assertFalse(inactiveBeforeThreshold.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE));
        }

        // Test pour vérifier la méthode countByCreationTimestampBetween.
        @Test
        void countByCreationTimestampBetween_shouldReturnCountOfSubscriptionsCreatedWithinGivenTimeRange() {
                // GIVEN : Création d'abonnements avec différentes dates de création.
                User user1 = createAndPersistUser(1L);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startOfYesterday = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime endOfYesterday = now.minusDays(1).withHour(23).withMinute(59).withSecond(59)
                                .withNano(999999999);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(2),
                                LocalDateTime.now(),
                                null, null, startOfYesterday.plusHours(1), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.PENDING, LocalDateTime.now().minusDays(1),
                                LocalDateTime.now(),
                                null, null, now.minusDays(1).withHour(12), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED, LocalDateTime.now().minusDays(3),
                                LocalDateTime.now(),
                                null, null, now.minusDays(2).withHour(20), null, null);

                // WHEN : Comptage des abonnements créés hier.
                long count = subscriptionRepository.countByCreationTimestampBetween(startOfYesterday, endOfYesterday);

                // THEN : Vérification que le compte correspond au nombre d'abonnements créés
                // hier.
                assertEquals(2, count);
        }

        // Test pour vérifier la méthode countByStatusAndCancellationDateBetween.
        @Test
        void countByStatusAndCancellationDateBetween_shouldReturnCountOfCancelledSubscriptionsCancelledWithinGivenDateRange() {
                // GIVEN : Création d'abonnements annulés avec différentes dates d'annulation.
                User user1 = createAndPersistUser(1L);
                Subscription subscription1 = createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1), null, null,
                                LocalDateTime.now(), null,
                                null);
                subscription1.setCancelledAt(LocalDateTime.now().minusDays(5));
                entityManager.persist(subscription1);
                Subscription subscription2 = createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2), null, null,
                                LocalDateTime.now(), null,
                                null);
                subscription2.setCancelledAt(LocalDateTime.now().minusDays(1));
                entityManager.persist(subscription2);
                Subscription subscription3 = createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(30), LocalDateTime.now(), null, null, LocalDateTime.now(),
                                null, null);
                entityManager.persist(subscription3);
                LocalDateTime startDate = LocalDateTime.now().minusDays(7);
                LocalDateTime endDate = LocalDateTime.now();
                SubscriptionStatus cancelledStatus = SubscriptionStatus.CANCELLED;

                // WHEN : Comptage des abonnements annulés dans la plage de dates.
                long count = subscriptionRepository.countByStatusAndCancellationDateBetween(cancelledStatus, startDate,
                                endDate);

                // THEN : Vérification que le compte correspond au nombre d'abonnements annulés
                // dans la plage.
                assertEquals(1, count);
        }

        // Test pour vérifier la méthode findByStatusAndLastPaymentDateBetween.
        @Test
        void findByStatusAndLastPaymentDateBetween_shouldReturnActiveSubscriptionsWithLastPaymentWithinGivenDateRange() {
                // GIVEN : Création d'abonnements actifs avec différentes dates de dernier
                // paiement.
                User user1 = createAndPersistUser(1L);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusMonths(1),
                                LocalDateTime.now(),
                                null, null, LocalDateTime.now(), LocalDateTime.now().minusDays(3), null);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(30),
                                LocalDateTime.now(),
                                null, null, LocalDateTime.now(), LocalDateTime.now().minusDays(10), null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED, LocalDateTime.now().minusMonths(2),
                                LocalDateTime.now().minusMonths(1), null, null, LocalDateTime.now(),
                                LocalDateTime.now().minusDays(2), null);
                LocalDateTime minusDays = LocalDateTime.now().minusDays(7);
                LocalDateTime localDate = LocalDateTime.now();
                SubscriptionStatus activeStatus = SubscriptionStatus.ACTIVE;

                // WHEN : Recherche des abonnements actifs dont la date de dernier paiement est
                // dans la plage.
                List<Subscription> activeWithPaymentInRange = subscriptionRepository
                                .findByStatusAndLastPaymentDateBetween(activeStatus, minusDays, localDate);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, activeWithPaymentInRange.size());
                assertTrue(activeWithPaymentInRange.stream()
                                .anyMatch(sub -> sub.getPaymentDate().isAfter(minusDays)
                                                && sub.getPaymentDate().isBefore(localDate.plusDays(1))));
        }

        // Test pour vérifier la méthode findSubscriptionsUpdatedSince.
        @Test
        void findSubscriptionsUpdatedSince_shouldReturnSubscriptionsUpdatedAfterGivenTimestampWithPagination() {
                // GIVEN : Création d'abonnements avec différentes dates de dernière activité.
                User user1 = createAndPersistUser(1L);
                LocalDateTime lastSyncTimestamp = LocalDateTime.now().minusDays(2);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(5),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null,
                                LocalDateTime.now().minusDays(1));
                Subscription subscription2 = createAndPersistSubscription(user1, SubscriptionStatus.PENDING,
                                LocalDateTime.now().minusDays(3), LocalDateTime.now(), null, null, LocalDateTime.now(),
                                null, LocalDateTime.now().minusDays(3));
                entityManager.persist(subscription2);
                Subscription subscription3 = createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED,
                                LocalDateTime.now().minusDays(7), LocalDateTime.now(), null, null, LocalDateTime.now(),
                                null, LocalDateTime.now());
                entityManager.persist(subscription3);

                // WHEN : Recherche des abonnements mis à jour après le timestamp donné avec
                // pagination.
                List<Subscription> updatedSubscriptions = subscriptionRepository
                                .findSubscriptionsUpdatedSince(lastSyncTimestamp, 0, 10);

                // THEN : Vérification que la liste contient les abonnements mis à jour après le
                // timestamp.
                assertEquals(2, updatedSubscriptions.size());
                assertTrue(updatedSubscriptions.stream()
                                .anyMatch(sub -> sub.getLastActivity().isAfter(lastSyncTimestamp)));
                assertTrue(updatedSubscriptions.stream().anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE));
                assertTrue(updatedSubscriptions.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.CANCELLED));
                assertFalse(updatedSubscriptions.stream()
                                .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.PENDING));
        }

        // Test pour vérifier la méthode findByStatusAndLastActivityBefore avec
        // pagination.
        @Test
        void findByStatusAndLastActivityBefore_shouldReturnSubscriptionsWithLastActivityBeforeGivenTimeWithPagination() {
                // GIVEN : Création d'abonnements avec différentes dates de dernière activité et
                // statuts.
                User user1 = createAndPersistUser(1L);
                LocalDateTime lastActivityBefore = LocalDateTime.now().minusDays(1);
                createAndPersistSubscription(user1, SubscriptionStatus.INACTIVE, LocalDateTime.now().minusDays(10),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null,
                                LocalDateTime.now().minusDays(2));
                createAndPersistSubscription(user1, SubscriptionStatus.INACTIVE, LocalDateTime.now().minusDays(5),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null,
                                LocalDateTime.now().minusDays(3));
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(2),
                                LocalDateTime.now(), null, null, LocalDateTime.now(), null, LocalDateTime.now());
                Pageable pageable = PageRequest.of(0, 1);
                Subscription.SubscriptionStatus inactiveStatus = Subscription.SubscriptionStatus.INACTIVE;

                // WHEN : Recherche des abonnements inactifs dont la dernière activité est
                // antérieure à la date donnée avec pagination.
                List<Subscription> inactiveBeforeTime = subscriptionRepository
                                .findByStatusAndLastActivityBefore(inactiveStatus, lastActivityBefore, pageable);

                // THEN : Vérification que la liste contient le nombre attendu d'éléments pour
                // la page demandée.
                assertEquals(1, inactiveBeforeTime.size());
                assertTrue(inactiveBeforeTime.stream()
                                .anyMatch(sub -> sub.getStatus() == Subscription.SubscriptionStatus.INACTIVE
                                                && sub.getLastActivity().isBefore(lastActivityBefore)));
        }

        // Test pour vérifier la méthode findByNextBillingDate.
        @Test
        void findByNextBillingDate_shouldReturnSubscriptionsWithGivenNextBillingDate() {
                // GIVEN : Création d'abonnements avec différentes dates de prochaine
                // facturation.
                User user1 = createAndPersistUser(1L);
                LocalDateTime reminderDate = LocalDateTime.now().plusDays(7);
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(30),
                                LocalDateTime.now().plusMonths(1), null, reminderDate, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.PENDING, LocalDateTime.now().minusDays(15),
                                LocalDateTime.now().plusDays(15), null, LocalDateTime.now().plusDays(10),
                                LocalDateTime.now(), null, null);

                // WHEN : Recherche des abonnements dont la prochaine date de facturation
                // correspond à la date de rappel.
                List<Subscription> billingOnReminderDate = subscriptionRepository.findByNextBillingDate(reminderDate);

                // THEN : Vérification que la liste contient l'abonnement attendu.
                assertEquals(1, billingOnReminderDate.size());
                assertTrue(billingOnReminderDate.stream()
                                .anyMatch(sub -> sub.getNextBillingDate().isEqual(reminderDate)));
        }

        // Test pour vérifier la méthode findByStripeSubscriptionId.
        @Test
        void findByStripeSubscriptionId_shouldReturnOptionalStripeObjectForGivenId() {
                // GIVEN : Création d'un abonnement et association d'un ID Stripe.
                User user1 = createAndPersistUser(1L);
                Subscription subscription = createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(10), LocalDateTime.now().plusMonths(1), null,
                                LocalDateTime.now().plusMonths(1), LocalDateTime.now(), null, null);
                String stripeSubscriptionId = "sub_123";
                // Simuler l'association de l'ID Stripe (la méthode dans le repository renvoie
                // StripeObject, mais on teste l'appel)
                subscription.setStripeSubscriptionId(stripeSubscriptionId);
                entityManager.persist(subscription);
                entityManager.flush();

                // WHEN : Recherche par ID Stripe.
                Optional<Subscription> foundSubscription = subscriptionRepository
                                .findByStripeSubscriptionId(stripeSubscriptionId);

                // THEN : Vérification que l'abonnement est trouvé et correspond à celui créé
                assertTrue(foundSubscription.isPresent());
                assertEquals(subscription, foundSubscription.get());
        }

        // Test pour vérifier la méthode findByEndDateBeforeAndStatusIn.
        @Test
        void findByEndDateBeforeAndStatusIn_shouldReturnSubscriptionsEndingBeforeGivenDateAndWithGivenStatuses() {
                // GIVEN : Création d'abonnements avec différentes dates de fin et statuts.
                User user1 = createAndPersistUser(1L);
                LocalDateTime today = LocalDateTime.now();
                createAndPersistSubscription(user1, SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(5),
                                today.minusDays(1), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.CANCELLED, LocalDateTime.now().minusDays(10),
                                today.minusDays(2), null, null, LocalDateTime.now(), null, null);
                createAndPersistSubscription(user1, SubscriptionStatus.PENDING, LocalDateTime.now().minusDays(1),
                                today.plusDays(1), null, null, LocalDateTime.now(), null, null);
                List<Object> statuses = Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED);
                LocalDateTime eq = today.minusDays(0); // Equivalent à today à 00:00:00

                // WHEN : Recherche des abonnements se terminant avant aujourd'hui et ayant les
                // statuts spécifiés.
                @SuppressWarnings("unchecked")
                List<Object> foundSubscriptions = (List<Object>) subscriptionRepository.findByEndDateBeforeAndStatusIn(
                                eq,
                                statuses);

                // THEN : Vérification que la liste contient les abonnements attendus.
                assertEquals(2, foundSubscriptions.size());
                assertTrue(foundSubscriptions.stream()
                                .anyMatch(sub -> ((Subscription) sub).getStatus() == SubscriptionStatus.ACTIVE));
                assertTrue(foundSubscriptions.stream()
                                .anyMatch(sub -> ((Subscription) sub).getStatus() == SubscriptionStatus.CANCELLED));
                assertFalse(foundSubscriptions.stream()
                                .anyMatch(sub -> ((Subscription) sub).getStatus() == SubscriptionStatus.PENDING));
                assertTrue(foundSubscriptions.stream()
                                .allMatch(sub -> ((Subscription) sub).getEndDate().isBefore(today)));
        }
}