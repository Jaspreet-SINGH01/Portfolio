package com.videoflix.subscriptions_microservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public void updateUserRole(Long userId, String newRole) {
        logger.info("L'administrateur a mis à jour le rôle de l'utilisateur {} vers {}", userId, newRole);
        // Logique de mise à jour du rôle
    }

    public void cancelSubscription(Long subscriptionId, Long adminUserId) {
        logger.warn("L'administrateur {} a annulé l'abonnement {}", adminUserId, subscriptionId);
        // Logique d'annulation d'abonnement
    }
}