package com.videoflix.subscriptions_microservice.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionManagementController.class) // Indique que nous testons le contrôleur
                                                    // SubscriptionManagementController
class SubscriptionManagementControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objet MockMvc pour simuler des requêtes HTTP

    // Test pour l'endpoint /free-content (accessible à tous)
    @Test
    void getFreeContent_shouldReturnOkAndFreeContent() throws Exception {
        // WHEN : On effectue une requête GET vers /free-content
        mockMvc.perform(MockMvcRequestBuilders.get("/free-content"))
                // THEN : On vérifie que la réponse a un statut OK (200)
                .andExpect(status().isOk())
                // AND : On vérifie que le contenu de la réponse est "Contenu accessible à
                // tous."
                .andExpect(content().string("Contenu accessible à tous."));
    }

    // Test pour l'endpoint /premium-content (nécessite l'autorité 'Accéder au
    // contenu Premium')
    @Test
    @WithMockUser(authorities = "Accéder au contenu Premium") // Simule un utilisateur avec l'autorité spécifiée
    void getPremiumContent_withPremiumAuthority_shouldReturnOkAndPremiumContent() throws Exception {
        // WHEN : On effectue une requête GET vers /premium-content en étant authentifié
        // avec l'autorité requise
        mockMvc.perform(MockMvcRequestBuilders.get("/premium-content"))
                // THEN : On vérifie que la réponse a un statut OK (200)
                .andExpect(status().isOk())
                // AND : On vérifie que le contenu de la réponse est "Contenu pour les abonnés
                // Premium et Ultra."
                .andExpect(content().string("Contenu pour les abonnés Premium et Ultra."));
    }

    @Test
    @WithMockUser // Simule un utilisateur authentifié mais sans l'autorité requise
    void getPremiumContent_withoutPremiumAuthority_shouldReturnForbidden() throws Exception {
        // WHEN : On effectue une requête GET vers /premium-content sans l'autorité
        // requise
        mockMvc.perform(MockMvcRequestBuilders.get("/premium-content"))
                // THEN : On vérifie que la réponse a un statut Forbidden (403)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATEUR") // Simule un utilisateur avec le rôle ADMINISTRATEUR
    void getUsersAdmin_withAdminRole_shouldReturnOkAndAdminInfo() throws Exception {
        // WHEN : On effectue une requête GET vers /admin/users en étant authentifié
        // avec le rôle requis
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users"))
                // THEN : On vérifie que la réponse a un statut OK (200)
                .andExpect(status().isOk())
                // AND : On vérifie que le contenu de la réponse est "Informations sensibles sur
                // les utilisateurs (admin only)."
                .andExpect(content().string("Informations sensibles sur les utilisateurs (admin only)."));
    }

    @Test
    @WithMockUser // Simule un utilisateur authentifié mais sans le rôle requis
    void getUsersAdmin_withoutAdminRole_shouldReturnForbidden() throws Exception {
        // WHEN : On effectue une requête GET vers /admin/users sans le rôle requis
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users"))
                // THEN : On vérifie que la réponse a un statut Forbidden (403)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Gérer les abonnements") // Simule un utilisateur avec l'autorité requise
    void manageSubscriptions_withManageSubscriptionsAuthority_shouldReturnOkAndManageInterface() throws Exception {
        // WHEN : On effectue une requête GET vers /manage/subscriptions en étant
        // authentifié avec l'autorité requise
        mockMvc.perform(MockMvcRequestBuilders.get("/manage/subscriptions"))
                // THEN : On vérifie que la réponse a un statut OK (200)
                .andExpect(status().isOk())
                // AND : On vérifie que le contenu de la réponse est "Interface de gestion des
                // abonnements."
                .andExpect(content().string("Interface de gestion des abonnements."));
    }

    @Test
    @WithMockUser // Simule un utilisateur authentifié mais sans l'autorité requise
    void manageSubscriptions_withoutManageSubscriptionsAuthority_shouldReturnForbidden() throws Exception {
        // WHEN : On effectue une requête GET vers /manage/subscriptions sans l'autorité
        // requise
        mockMvc.perform(MockMvcRequestBuilders.get("/manage/subscriptions"))
                // THEN : On vérifie que la réponse a un statut Forbidden (403)
                .andExpect(status().isForbidden());
    }
}