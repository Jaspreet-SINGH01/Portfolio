package com.videoflix.subscriptions_microservice.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionManagementController {

    @GetMapping("/free-content")
    public String getFreeContent() {
        return "Contenu accessible à tous.";
    }

    @GetMapping("/premium-content")
    @PreAuthorize("hasAuthority('Accéder au contenu Premium')")
    public String getPremiumContent() {
        return "Contenu pour les abonnés Premium et Ultra.";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMINISTRATEUR')") // Utilisation du rôle
    public String getUsersAdmin() {
        return "Informations sensibles sur les utilisateurs (admin only).";
    }

    @GetMapping("/manage/subscriptions")
    @PreAuthorize("hasAuthority('Gérer les abonnements')") // Utilisation de la permission
    public String manageSubscriptions() {
        return "Interface de gestion des abonnements.";
    }
}