package com.videoflix.subscriptions_microservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // @Mock crée un mock de l'interface Logger.
    @Mock
    private Logger logger;

    // @InjectMocks crée une instance de AdminService et injecte le mock annoté avec
    // @Mock.
    @InjectMocks
    private AdminService adminService;

    // Test pour vérifier que la méthode updateUserRole enregistre correctement un
    // message d'information.
    @Test
    void updateUserRole_shouldLogUpdateRoleAction() {
        // GIVEN : Un ID utilisateur et un nouveau rôle.
        Long userId = 123L;
        String newRole = "ROLE_ADMIN";

        // WHEN : Appel de la méthode updateUserRole.
        adminService.updateUserRole(userId, newRole);

        // THEN : Vérification que la méthode info du logger a été appelée une fois avec
        // le message attendu.
        verify(logger, times(1)).info(
                "L'administrateur a mis à jour le rôle de l'utilisateur {} vers {}",
                userId,
                newRole);
    }

    // Test pour vérifier que la méthode cancelSubscription enregistre correctement
    // un message d'avertissement.
    @Test
    void cancelSubscription_shouldLogCancelSubscriptionAction() {
        // GIVEN : Un ID d'abonnement et un ID d'utilisateur administrateur.
        Long subscriptionId = 456L;
        Long adminUserId = 789L;

        // WHEN : Appel de la méthode cancelSubscription.
        adminService.cancelSubscription(subscriptionId, adminUserId);

        // THEN : Vérification que la méthode warn du logger a été appelée une fois avec
        // le message attendu.
        verify(logger, times(1)).warn(
                "L'administrateur {} a annulé l'abonnement {}",
                adminUserId,
                subscriptionId);
    }
}