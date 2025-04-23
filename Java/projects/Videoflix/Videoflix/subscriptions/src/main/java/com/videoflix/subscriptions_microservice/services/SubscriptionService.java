package com.videoflix.subscriptions_microservice.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.videoflix.subscriptions_microservice.dtos.AdminUpdateSubscriptionRequest;
import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.integration.AccessControlEventPublisher;
import com.videoflix.subscriptions_microservice.integration.SubscriptionCancelledEventPublisher;
import com.videoflix.subscriptions_microservice.integration.SubscriptionLevelChangedEventPublisher;
import com.videoflix.subscriptions_microservice.integration.SubscriptionReactivatedEventPublisher;
import com.videoflix.subscriptions_microservice.integration.SubscriptionRenewedEventPublisher;
import com.videoflix.subscriptions_microservice.repositories.PromotionRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionLevelRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with ID : ";
    private static final String SUBSCRIPTION_NOT_FOUND_MESSAGE = "Subscription not found";
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionLevelRepository subscriptionLevelRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private SubscriptionLevelChangedEventPublisher levelChangedEventPublisher;
    private SubscriptionCancelledEventPublisher subscriptionCancelledEventPublisher;
    private final BillingCalculationService billingCalculationService;
    private final SubscriptionRenewedEventPublisher subscriptionRenewedEventPublisher;
    private final SubscriptionReactivatedEventPublisher subscriptionReactivatedEventPublisher;
    private final AccessControlEventPublisher accessControlEventPublisher;
    private final StripeBillingService stripeBillingService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
            SubscriptionLevelRepository subscriptionLevelRepository,
            PromotionRepository promotionRepository,
            UserRepository userRepository,
            EntityManager entityManager,
            SubscriptionLevelChangedEventPublisher levelChangedEventPublisher,
            SubscriptionCancelledEventPublisher subscriptionCancelledEventPublisher,
            BillingCalculationService billingCalculationService,
            SubscriptionRenewedEventPublisher subscriptionRenewedEventPublisher,
            SubscriptionReactivatedEventPublisher subscriptionReactivatedEventPublisher,
            AccessControlEventPublisher accessControlEventPublisher,
            StripeBillingService stripeBillingService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionLevelRepository = subscriptionLevelRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.levelChangedEventPublisher = levelChangedEventPublisher;
        this.subscriptionCancelledEventPublisher = subscriptionCancelledEventPublisher;
        this.billingCalculationService = billingCalculationService;
        this.subscriptionRenewedEventPublisher = subscriptionRenewedEventPublisher;
        this.subscriptionReactivatedEventPublisher = subscriptionReactivatedEventPublisher;
        this.accessControlEventPublisher = accessControlEventPublisher;
        this.stripeBillingService = stripeBillingService;
    }

    // Méthodes pour gérer les abonnements

    public Subscription createSubscription(Subscription subscription) {
        validateSubscription(subscription);

        return subscriptionRepository.save(subscription); // Crée un nouvel abonnement et le sauvegarde dans la base de
                                                          // données
    }

    public Subscription getSubscriptionById(Long id) {
        // Récupère un abonnement par son ID
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id);
        return subscriptionOptional.orElse(null);
    }

    public List<Subscription> getAllSubscriptions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(pageable);
        return subscriptionPage.getContent();
    }

    public Subscription updateSubscription(Long id, Subscription subscription) {
        // Met à jour un abonnement existant
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id);
        if (subscriptionOptional.isPresent()) {
            subscription.setId(id); // Assure que l'ID de l'abonnement mis à jour est correct
            return subscriptionRepository.save(subscription);
        }
        return null; // Retourne null si l'abonnement n'existe pas
    }

    public Subscription changeSubscriptionLevel(Long subscriptionId, Long newSubscriptionLevelId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(subscriptionId);
        Optional<SubscriptionLevel> newSubscriptionLevelOptional = subscriptionLevelRepository
                .findById(newSubscriptionLevelId);

        if (subscriptionOptional.isPresent() && newSubscriptionLevelOptional.isPresent()) {
            Subscription subscription = subscriptionOptional.get();
            subscription.setSubscriptionLevel(newSubscriptionLevelOptional.get());
            return subscriptionRepository.save(subscription);
        }
        return null; // Retourne null si l'abonnement ou le nouveau niveau n'existe pas
    }

    public void deleteSubscription(Long id) {
        // Supprime un abonnement par son ID
        subscriptionRepository.deleteById(id);
    }

    // Méthodes pour gérer les niveaux d'abonnement

    public SubscriptionLevel createSubscriptionLevel(SubscriptionLevel level) {
        // Crée un nouveau niveau d'abonnement
        return subscriptionLevelRepository.save(level);
    }

    public SubscriptionLevel getSubscriptionLevelById(Long id) {
        // Récupère un niveau d'abonnement par son ID
        Optional<SubscriptionLevel> levelOptional = subscriptionLevelRepository.findById(id);
        return levelOptional.orElse(null);
    }

    public List<SubscriptionLevel> getAllSubscriptionLevels() {
        // Récupère tous les niveaux d'abonnement
        return subscriptionLevelRepository.findAll();
    }

    public SubscriptionLevel updateSubscriptionLevel(Long id, SubscriptionLevel level) {
        // Met à jour un niveau d'abonnement existant
        Optional<SubscriptionLevel> levelOptional = subscriptionLevelRepository.findById(id);
        if (levelOptional.isPresent()) {
            level.setId(id); // Assure que l'ID du niveau est correct
            return subscriptionLevelRepository.save(level);
        }
        return null; // Retourne null si le niveau n'existe pas
    }

    public void deleteSubscriptionLevel(Long id) {
        // Supprime un niveau d'abonnement par son ID
        subscriptionLevelRepository.deleteById(id);
    }

    // Promotions

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion getPromotionByCode(String code) {
        return promotionRepository.findByPromotionCode(code);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Subscription applyPromotion(Long subscriptionId, String promotionCode) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(subscriptionId);
        Promotion promotion = promotionRepository.findByPromotionCode(promotionCode);

        if (subscriptionOptional.isPresent() && promotion != null && promotion.isActive() &&
                promotion.getStartDate().isBefore(LocalDateTime.now()) &&
                promotion.getEndDate().isAfter(LocalDateTime.now())) {
            Subscription subscription = subscriptionOptional.get();
            subscription.setPromotion(promotion);
            return subscriptionRepository.save(subscription);
        }
        return null; // Retourne null si l'abonnement ou la promotion n'existe pas ou n'est pas
                     // valide
    }

    @Transactional
    public void processSubscriptionRenewal(Long userId, String userEmail, String subscriptionLevel, String pushToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        Subscription activeSubscription = userSubscriptions.stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active subscription found for user: " + userId));

        activeSubscription.setEndDate(activeSubscription.getEndDate().plusMonths(1));
        activeSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(activeSubscription);
        EmailService emailService = new EmailService(null);
        // Logique de renouvellement de l'abonnement
        emailService.sendSubscriptionRenewalNotification(userEmail, subscriptionLevel);
        if (pushToken != null && !pushToken.isEmpty()) {
            PushNotificationService pushNotificationService = new PushNotificationService(null);
            pushNotificationService.sendSubscriptionRenewalPushNotification(pushToken, subscriptionLevel);
        }
    }

    @Transactional
    public void checkSubscriptionExpirations() {
        LocalDateTime today = LocalDateTime.now();

        // Rechercher les abonnements qui expirent dans 3 jours
        List<Subscription> expiringSoon = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE
                        && sub.getEndDate().isEqual(today.plusDays(3)))
                .toList();

        expiringSoon.forEach(subscription -> {
            User user = subscription.getUser();
            EmailService emailService = new EmailService(null);
            emailService.sendSubscriptionExpirationImminentNotification(user.getEmail(),
                    subscription.getSubscriptionLevel(), subscription.getEndDate());

            // Envoyer une notification push si le token est disponible
            if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
                PushNotificationService pushNotificationService = new PushNotificationService(null);
                pushNotificationService.sendSubscriptionExpirationImminentPushNotification(user.getPushToken(),
                        subscription.getSubscriptionLevel(), subscription.getEndDate());
            }
        });

        // Rechercher les abonnements expirés
        List<Subscription> expired = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE
                        && sub.getEndDate().isBefore(today))
                .toList();

        expired.forEach(subscription -> {
            subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            User user = subscription.getUser();
            EmailService emailService = new EmailService(null);
            emailService.sendSubscriptionExpirationImminentNotification(user.getEmail(),
                    subscription.getSubscriptionLevel(), subscription.getEndDate());

            // Envoyer une notification push pour l'expiration
            if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
                PushNotificationService pushNotificationService2 = new PushNotificationService(null);
                pushNotificationService2.sendSubscriptionExpirationPushNotification(user.getPushToken(),
                        subscription.getSubscriptionLevel());
            }
        });
    }

    @Transactional
    public void handlePaymentFailure(Long userId, String userEmail, String subscriptionLevel, String pushToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        Subscription subscriptionToUpdate = userSubscriptions.stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE
                        || sub.getStatus() == Subscription.SubscriptionStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (subscriptionToUpdate != null) {
            subscriptionToUpdate.setStatus(Subscription.SubscriptionStatus.PAYMENT_FAILED);
            subscriptionRepository.save(subscriptionToUpdate);
            EmailService emailService = new EmailService(null);
            emailService.sendPaymentFailedNotification(userEmail, subscriptionLevel);
            if (pushToken != null && !pushToken.isEmpty()) {
                PushNotificationService pushNotificationService = new PushNotificationService(null);
                pushNotificationService.sendPaymentFailedPushNotification(pushToken, subscriptionLevel);
            }
        }
    }

    @Transactional
    public void handlePaymentSuccess(Long userId, String userEmail, String subscriptionLevel, String pushToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        Subscription subscriptionToUpdate = userSubscriptions.stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.PENDING
                        || sub.getStatus() == Subscription.SubscriptionStatus.PAYMENT_FAILED)
                .findFirst()
                .orElseGet(() -> userSubscriptions.stream().max((s1, s2) -> s1.getEndDate().compareTo(s2.getEndDate()))
                        .orElse(null));

        if (subscriptionToUpdate != null) {
            subscriptionToUpdate.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            if (!subscriptionToUpdate.getStartDate().isBefore(LocalDateTime.now())) {
                subscriptionToUpdate.setStartDate(LocalDateTime.now());
                subscriptionToUpdate.setEndDate(LocalDateTime.now().plusMonths(1));
            } else {
                subscriptionToUpdate.setEndDate(subscriptionToUpdate.getEndDate().plusMonths(1));
            }
            subscriptionRepository.save(subscriptionToUpdate);
            EmailService emailService = new EmailService(null);
            emailService.sendPaymentRetrySuccessNotification(userEmail, subscriptionLevel);
            if (pushToken != null && !pushToken.isEmpty()) {
                PushNotificationService pushNotificationService = new PushNotificationService(null);
                pushNotificationService.sendPaymentRetrySuccessPushNotification(pushToken, subscriptionLevel);
            }
        } else {
            logger.error("No subscription found for user {} on payment success.", userId);
        }
    }

    
    public Subscription createNewSubscription(Long userId, SubscriptionLevel subscriptionLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));

        try {
            // Récupérer l'ID du prix Stripe associé au SubscriptionLevel
            String stripePriceId = subscriptionLevel.getStripePriceId();
            if (stripePriceId == null) {
                throw new IllegalStateException("L'ID de prix Stripe n'est pas configuré pour le niveau d'abonnement : " + subscriptionLevel.getLevel());
            }

            // Créer l'abonnement sur Stripe
            StripeSubscription stripeSubscription = stripeBillingService.createSubscription(user.getStripeCustomerId(), stripePriceId);

            String stripeChargeId = null;
            // Tenter de récupérer l'ID de la charge du premier paiement
            // Cela dépend de la façon dont Stripe a configuré votre abonnement (paiement immédiat ou non)
            if (stripeSubscription.getLatestCharge() != null) {
                stripeChargeId = stripeSubscription.getLatestCharge().getId();
            } else if (stripeSubscription.getLatestInvoice() != null && stripeSubscription.getLatestInvoice().getPaymentIntentObject() != null && stripeSubscription.getLatestInvoice().getPaymentIntentObject().getLatestCharge() != null) {
                stripeChargeId = stripeSubscription.getLatestInvoice().getPaymentIntentObject().getLatestCharge().getId();
            }

            Subscription newSubscription = new Subscription();
            newSubscription.setUser(user);
            newSubscription.setStartDate(LocalDateTime.now());
            // La date de fin sera gérée par Stripe ou calculée en fonction de la fréquence
            newSubscription.setSubscriptionLevel(subscriptionLevel);
            newSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            newSubscription.setStripeSubscriptionId(stripeSubscription.getId());
            newSubscription.setStripeChargeId(stripeChargeId); // Enregistrer l'ID de la charge
            newSubscription.setPrice(subscriptionLevel.getPrice()); // Récupérer le prix du niveau
            newSubscription.setCurrency(stripeSubscription.getCurrency()); // Récupérer la devise de Stripe

            // Initialiser la prochaine date de facturation (peut être ajustée après le premier paiement)
            if (stripeSubscription.getCurrentPeriodEnd() != null) {
                newSubscription.setNextBillingDate(LocalDateTime.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd(), 0, java.time.ZoneOffset.UTC));
            } else {
                // Fallback si la date de fin de la période n'est pas immédiatement disponible
                newSubscription.setNextBillingDate(LocalDateTime.now().plus(1, java.time.temporal.ChronoUnit.MONTHS)); // Exemple par défaut
            }

            return subscriptionRepository.save(newSubscription);

        } catch (StripeException e) {
            throw new RuntimeException("Erreur lors de la création de l'abonnement Stripe : " + e.getMessage());
        }
    }

    // Historique des abonnements
    public List<Object[]> getSubscriptionHistory(Long subscriptionId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> result = reader.createQuery()
                .forRevisionsOfEntity(Subscription.class, false, true)
                .add(AuditEntity.id().eq(subscriptionId))
                .getResultList();
        return result;
    }

    @Transactional
    public Subscription updateSubscriptionByAdmin(Long id, AdminUpdateSubscriptionRequest updateRequest) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id);
        if (subscriptionOptional.isPresent()) {
            Subscription subscription = subscriptionOptional.get();

            if (updateRequest.getStatus() != null) {
                subscription.setStatus(updateRequest.getStatus());
            }
            if (updateRequest.getStartDate() != null) {
                subscription.setStartDate(updateRequest.getStartDate());
            }
            if (updateRequest.getEndDate() != null) {
                subscription.setEndDate(updateRequest.getEndDate());
            }
            if (updateRequest.getSubscriptionLevelId() != null) {
                Optional<SubscriptionLevel> levelOptional = subscriptionLevelRepository
                        .findById(updateRequest.getSubscriptionLevelId());
                levelOptional.ifPresent(subscription::setSubscriptionLevel);
            }

            return subscriptionRepository.save(subscription);
        }
        return null;
    }

    private void validateSubscription(Subscription subscription) {
        if (subscription.getUser() == null || subscription.getUser().getId() == null
                || !userRepository.existsById(subscription.getUser().getId())) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'existe pas.");
        }
        if (subscription.getStartDate() == null || subscription.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de début doit être aujourd'hui ou dans le futur.");
        }
        if (subscription.getEndDate() != null && subscription.getStartDate() != null
                && subscription.getEndDate().isBefore(subscription.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        if (subscription.getSubscriptionLevel() == null || subscription.getSubscriptionLevel().getLevel() == null) {
            throw new IllegalArgumentException("Le type d'abonnement ne peut pas être vide.");
        }
        if (subscription.getStatus() == null) {
            throw new IllegalArgumentException("Le statut de l'abonnement doit être spécifié.");
        }
    }

    public List<Subscription> searchSubscriptions(Long userId, String status, String subscriptionLevel, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Subscription> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (subscriptionLevel != null && !subscriptionLevel.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionLevel"), subscriptionLevel));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(spec, pageable);
        return subscriptionPage.getContent();
    }

    public List<Subscription> getAllSubscriptionsByFilters(Long userId, String status, String subscriptionLevel,
            int page, int size) {
        return searchSubscriptions(userId, status, subscriptionLevel, page, size);
    }

    public void changeSubscriptionLevel(Long subscriptionId, String newLevelName) {
        // Récupérer l'abonnement existant
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));

        SubscriptionLevel oldSubscriptionLevel = subscription.getSubscriptionLevel();
        String oldLevelString = (oldSubscriptionLevel != null)
                ? oldSubscriptionLevel.getLevel().name()
                : null;

        // Trouver le nouveau niveau d'abonnement
        SubscriptionLevel newSubscriptionLevel = findSubscriptionLevelByName(newLevelName);
        String newLevelString = newSubscriptionLevel.getLevel().name();

        // Vérifier si le niveau est déjà le même → pas de mise à jour inutile
        if (oldLevelString != null && oldLevelString.equalsIgnoreCase(newLevelString)) {
            logger.info("Aucun changement de niveau pour l'abonnement ID {} (niveau déjà '{}')",
                    subscriptionId, oldLevelString);
            return; // rien à faire
        }

        // Appliquer le changement
        subscription.setSubscriptionLevel(newSubscriptionLevel);
        subscriptionRepository.save(subscription);

        // Log du changement
        logger.info("Changement du niveau d'abonnement pour ID {} : {} → {}",
                subscriptionId, oldLevelString, newLevelString);

        // Publier l’Évènement
        levelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription, oldLevelString);
    }

    /**
     * Recherche un niveau d'abonnement en fonction de son nom.
     *
     * @param levelName Nom du niveau (ex: "BASIC", "PREMIUM", etc.)
     * @return L'entité SubscriptionLevel correspondante
     * @throws IllegalArgumentException si le niveau est invalide ou introuvable
     */
    public SubscriptionLevel findSubscriptionLevelByName(String levelName) {
        try {
            SubscriptionLevel.Level levelEnum = SubscriptionLevel.Level.fromString(levelName);
            return subscriptionLevelRepository.findByLevel(levelEnum)
                    .orElseThrow(() -> new IllegalArgumentException("Niveau d'abonnement introuvable : " + levelName));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Niveau d'abonnement invalide ou introuvable : " + levelName, e);
        }
    }

    public void cancelSubscription(Long subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED); // Mettre à jour le statut
        subscription.setCancelledAt(java.time.LocalDateTime.now()); // Enregistrer la date d'annulation
        subscriptionRepository.save(subscription);
        subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription, reason);

        // Informer le service de gestion des accès de l'annulation
        if (subscription.getUser() != null) {
            accessControlEventPublisher.publishSubscriptionCancelledForAccessControl(
                    subscription.getUser().getId(),
                    subscription.getSubscriptionLevel().getLevel().name(), // Envoyer le nom du niveau
                    reason);
            logger.info(
                    "Évènement d'annulation d'abonnement publié pour le contrôle d'accès (utilisateur ID {}, niveau '{}', raison '{}').",
                    subscription.getUser().getId(), subscription.getSubscriptionLevel().getLevel().name(), reason);
        } else {
            logger.warn(
                    "Utilisateur non trouvé pour l'abonnement ID {}, impossible de publier l'événement de contrôle d'accès pour l'annulation.",
                    subscriptionId);
        }

        if (shouldProcessRefund(subscription, reason)) {
            try {
                String chargeId = subscription.getStripeChargeId(); // Supposons que vous stockiez l'ID de la charge dans l'entité Subscription
                if (chargeId != null) {
                    Refund refund = stripeBillingService.refundCharge(chargeId, "Subscription cancelled: " + reason);
                    logger.info("Remboursement Stripe {} initié pour la charge {} suite à l'annulation de l'abonnement ID {}",
                                refund.getId(), chargeId, subscriptionId);
                    // Mettre à jour le statut du remboursement dans votre base de données si nécessaire
                } else {
                    logger.warn("Impossible de trouver l'ID de charge Stripe pour l'abonnement ID {}", subscriptionId);
                }
            } catch (StripeException e) {
                logger.error("Erreur lors du traitement du remboursement pour l'abonnement ID {} : {}", subscriptionId, e.getMessage());
            }
        }
    }

    // Implémentez votre logique de politique de remboursement ici
    private boolean shouldProcessRefund(Subscription subscription, String reason) {
        // Exemple de politique : rembourser si l'annulation a lieu dans les 7 jours suivant le début de l'abonnement
        return subscription.getStartDate().isAfter(java.time.LocalDateTime.now().minusDays(7));
    }

    // Vous devrez également vous assurer que votre entité Subscription a un champ stripeChargeId
    // et que ce champ est renseigné lors de la création de l'abonnement.

    public void processSuccessfulRenewal(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));
        // Mettre à jour la date de prochaine facturation et potentiellement d'autres
        // informations
        subscription.setNextBillingDate(billingCalculationService.calculateNextBillingDate(subscription));
        subscriptionRepository.save(subscription);
        subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);
    }

    public void reactivateSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE); // Mettre à jour le statut
        // Potentiellement recalculer la prochaine date de facturation
        subscription
                .setNextBillingDate(billingCalculationService.calculateNextBillingDateAfterReactivation(subscription));
        subscriptionRepository.save(subscription);
        subscriptionReactivatedEventPublisher.publishSubscriptionReactivatedEvent(subscription);

        // Publier un événement pour informer le service de gestion des accès
        if (subscription.getUser() != null) {
            accessControlEventPublisher.publishSubscriptionReactivatedForAccessControl(
                    subscription.getUser().getId(),
                    subscription.getSubscriptionLevel().getLevel().name() // Envoyer le nom du niveau
            );
            logger.info(
                    "Événement de réactivation d'abonnement publié pour le contrôle d'accès (utilisateur ID {}, niveau '{}').",
                    subscription.getUser().getId(), subscription.getSubscriptionLevel().getLevel().name());
        } else {
            logger.warn(
                    "Utilisateur non trouvé pour l'abonnement ID {}, impossible de publier l'événement de contrôle d'accès.",
                    subscriptionId);
        }
    }
}