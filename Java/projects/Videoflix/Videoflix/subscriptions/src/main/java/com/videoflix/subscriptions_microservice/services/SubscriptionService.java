package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.repositories.PromotionRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionLevelRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionLevelRepository subscriptionLevelRepository;
    private final PromotionRepository promotionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
            SubscriptionLevelRepository subscriptionLevelRepository,
            PromotionRepository promotionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionLevelRepository = subscriptionLevelRepository;
        this.promotionRepository = promotionRepository;
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

    // Ajouter d'autres méthodes de logique métier ici, par exemple :
    // - Calculer le prix de l'abonnement en fonction du niveau
    // - Gérer les renouvellements
    // - Envoyer des notifications aux utilisateurs
}