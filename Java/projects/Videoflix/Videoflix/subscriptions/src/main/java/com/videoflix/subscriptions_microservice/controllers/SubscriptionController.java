package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RestTemplate restTemplate;

    public SubscriptionController(SubscriptionService subscriptionService, RestTemplate restTemplate) {
        this.subscriptionService = subscriptionService;
        this.restTemplate = restTemplate;
    }

    // Gestion des abonnements

    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        // Récupère tous les abonnements
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        // Récupère un abonnement par son ID
        Subscription subscription = subscriptionService.getSubscriptionById(id);
        if (subscription != null) {
            return ResponseEntity.ok(subscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@RequestBody Subscription subscription,
            @RequestParam Long userId) {
        // Crée un nouvel abonnement et met à jour le subscriptionId de l'utilisateur
        // dans le microservice users

        // Crée l'abonnement
        Subscription createdSubscription = subscriptionService.createSubscription(subscription);

        // Met à jour le subscriptionId de l'utilisateur dans le microservice users
        restTemplate.put("http://users-microservice/users/" + userId + "/subscription", createdSubscription.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Long id,
            @RequestBody Subscription subscription) {
        // Met à jour un abonnement existant
        Subscription updatedSubscription = subscriptionService.updateSubscription(id, subscription);
        if (updatedSubscription != null) {
            return ResponseEntity.ok(updatedSubscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/level")
    public ResponseEntity<Subscription> changeSubscriptionLevel(@PathVariable Long id,
            @RequestParam Long newSubscriptionLevelId) {
        Subscription updatedSubscription = subscriptionService.changeSubscriptionLevel(id, newSubscriptionLevelId);
        if (updatedSubscription != null) {
            return ResponseEntity.ok(updatedSubscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        // Supprime un abonnement par son ID
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    // Gestion des niveaux d'abonnement

    @PostMapping("/levels")
    public ResponseEntity<SubscriptionLevel> createSubscriptionLevel(@RequestBody SubscriptionLevel level) {
        // Crée un nouveau niveau d'abonnement
        SubscriptionLevel createdLevel = subscriptionService.createSubscriptionLevel(level);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLevel);
    }

    @GetMapping("/levels/{id}")
    public ResponseEntity<SubscriptionLevel> getSubscriptionLevelById(@PathVariable Long id) {
        // Récupère un niveau d'abonnement par son ID
        SubscriptionLevel level = subscriptionService.getSubscriptionLevelById(id);
        if (level != null) {
            return ResponseEntity.ok(level);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/levels")
    public ResponseEntity<List<SubscriptionLevel>> getAllSubscriptionLevels() {
        // Récupère tous les niveaux d'abonnement
        List<SubscriptionLevel> levels = subscriptionService.getAllSubscriptionLevels();
        return ResponseEntity.ok(levels);
    }

    @PutMapping("/levels/{id}")
    public ResponseEntity<SubscriptionLevel> updateSubscriptionLevel(@PathVariable Long id,
            @RequestBody SubscriptionLevel level) {
        // Met à jour un niveau d'abonnement existant
        SubscriptionLevel updatedLevel = subscriptionService.updateSubscriptionLevel(id, level);
        if (updatedLevel != null) {
            return ResponseEntity.ok(updatedLevel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<Void> deleteSubscriptionLevel(@PathVariable Long id) {
        // Supprime un niveau d'abonnement par son ID
        subscriptionService.deleteSubscriptionLevel(id);
        return ResponseEntity.noContent().build();
    }

    // Promotions

    @PostMapping("/promotions")
    public ResponseEntity<Promotion> createPromotion(@RequestBody Promotion promotion) {
        Promotion createdPromotion = subscriptionService.createPromotion(promotion);
        return ResponseEntity.ok(createdPromotion);
    }

    @GetMapping("/promotions/{code}")
    public ResponseEntity<Promotion> getPromotionByCode(@PathVariable String code) {
        Promotion promotion = subscriptionService.getPromotionByCode(code);
        if (promotion != null) {
            return ResponseEntity.ok(promotion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        List<Promotion> promotions = subscriptionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }

    @PutMapping("/{id}/promotion")
    public ResponseEntity<Subscription> applyPromotion(@PathVariable Long id, @RequestParam String promotionCode) {
        Subscription updatedSubscription = subscriptionService.applyPromotion(id, promotionCode);
        if (updatedSubscription != null) {
            return ResponseEntity.ok(updatedSubscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}