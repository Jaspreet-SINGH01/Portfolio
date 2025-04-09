package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.repositories.PromotionRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionLevelRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with ID : ";
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionLevelRepository subscriptionLevelRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
            SubscriptionLevelRepository subscriptionLevelRepository,
            PromotionRepository promotionRepository,
            UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionLevelRepository = subscriptionLevelRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
    }

    // Méthodes pour gérer les abonnements

    public Subscription createSubscription(Subscription subscription) {
        // Crée un nouvel abonnement et le sauvegarde dans la base de données
        return subscriptionRepository.save(subscription);
    }

    public Subscription getSubscriptionById(Long id) {
        // Récupère un abonnement par son ID
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id);
        return subscriptionOptional.orElse(null);
    }

    public List<Subscription> getAllSubscriptions() {
        // Récupère tous les abonnements
        return subscriptionRepository.findAll();
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
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE) // Utilisez l'enum
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active subscription found for user: " + userId));

        activeSubscription.setEndDate(activeSubscription.getEndDate().plusMonths(1));
        activeSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE); // Utilisez l'enum
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
    public void handlePaymentFailure(Long userId, String userEmail, String subscriptionType, String pushToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        Subscription subscriptionToUpdate = userSubscriptions.stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE
                        || sub.getStatus() == Subscription.SubscriptionStatus.PENDING) // Utilisez l'enum
                .findFirst()
                .orElse(null);

        if (subscriptionToUpdate != null) {
            subscriptionToUpdate.setStatus(Subscription.SubscriptionStatus.PAYMENT_FAILED); // Utilisez l'enum
            subscriptionRepository.save(subscriptionToUpdate);
            EmailService emailService = new EmailService(null);
            emailService.sendPaymentFailedNotification(userEmail, subscriptionType);
            if (pushToken != null && !pushToken.isEmpty()) {
                PushNotificationService pushNotificationService = new PushNotificationService(null);
                pushNotificationService.sendPaymentFailedPushNotification(pushToken, subscriptionType);
            }
        }
    }

    @Transactional
    public void handlePaymentSuccess(Long userId, String userEmail, String subscriptionType, String pushToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        Subscription subscriptionToUpdate = userSubscriptions.stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.PENDING
                        || sub.getStatus() == Subscription.SubscriptionStatus.PAYMENT_FAILED) // Utilisez l'enum
                .findFirst()
                .orElseGet(() -> userSubscriptions.stream().max((s1, s2) -> s1.getEndDate().compareTo(s2.getEndDate()))
                        .orElse(null));

        if (subscriptionToUpdate != null) {
            subscriptionToUpdate.setStatus(Subscription.SubscriptionStatus.ACTIVE); // Utilisez l'enum
            if (!subscriptionToUpdate.getStartDate().isBefore(LocalDateTime.now())) {
                subscriptionToUpdate.setStartDate(LocalDateTime.now());
                subscriptionToUpdate.setEndDate(LocalDateTime.now().plusMonths(1));
            } else {
                subscriptionToUpdate.setEndDate(subscriptionToUpdate.getEndDate().plusMonths(1));
            }
            subscriptionRepository.save(subscriptionToUpdate);
            EmailService emailService = new EmailService(null);
            emailService.sendPaymentRetrySuccessNotification(userEmail, subscriptionType);
            if (pushToken != null && !pushToken.isEmpty()) {
                PushNotificationService pushNotificationService = new PushNotificationService(null);
                pushNotificationService.sendPaymentRetrySuccessPushNotification(pushToken, subscriptionType);
            }
        } else {
            logger.error("No subscription found for user {} on payment success.", userId);
        }
    }

    @Transactional
    public Subscription createNewSubscription(Long userId, SubscriptionLevel subscriptionLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE + userId));
        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setStartDate(LocalDateTime.now());
        newSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
        newSubscription.setSubscriptionLevel(subscriptionLevel);
        newSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(newSubscription);
    }
}