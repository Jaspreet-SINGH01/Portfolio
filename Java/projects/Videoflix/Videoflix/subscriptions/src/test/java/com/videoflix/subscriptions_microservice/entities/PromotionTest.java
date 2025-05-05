package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {

    // Test pour vérifier la création et la récupération des attributs d'une
    // promotion
    @Test
    void promotion_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs pour les attributs de la promotion
        String promotionCode = "SUMMER20";
        int discountPercentage = 20;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(3);
        boolean active = true;

        // WHEN : Création d'une instance de Promotion et définition de ses attributs
        Promotion promotion = new Promotion();
        promotion.setPromotionCode(promotionCode);
        promotion.setDiscountPercentage(discountPercentage);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setActive(active);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(promotion.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(promotionCode, promotion.getPromotionCode(), "Le code promo doit correspondre.");
        assertEquals(discountPercentage, promotion.getDiscountPercentage(),
                "Le pourcentage de réduction doit correspondre.");
        assertEquals(startDate, promotion.getStartDate(), "La date de début doit correspondre.");
        assertEquals(endDate, promotion.getEndDate(), "La date de fin doit correspondre.");
        assertEquals(active, promotion.isActive(), "L'état actif doit correspondre.");
    }

    // Test pour vérifier que les attributs d'une promotion peuvent être modifiés
    @Test
    void promotion_shouldAllowAttributeModification() {
        // GIVEN : Création d'une instance de Promotion et définition de valeurs
        // initiales
        Promotion promotion = new Promotion();
        promotion.setPromotionCode("OLDCODE");
        promotion.setDiscountPercentage(10);
        promotion.setActive(false);

        // WHEN : Modification des attributs de la promotion
        String newCode = "NEWCODE";
        int newDiscount = 25;
        boolean newActive = true;
        promotion.setPromotionCode(newCode);
        promotion.setDiscountPercentage(newDiscount);
        promotion.setActive(newActive);

        // THEN : Vérification que les attributs ont été correctement modifiés
        assertEquals(newCode, promotion.getPromotionCode(), "Le code promo devrait avoir été modifié.");
        assertEquals(newDiscount, promotion.getDiscountPercentage(),
                "Le pourcentage de réduction devrait avoir été modifié.");
        assertEquals(newActive, promotion.isActive(), "L'état actif devrait avoir été modifié.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void promotion_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de Promotion
        Promotion promotion = new Promotion();

        // THEN : Vérification que l'ID est null
        assertNull(promotion.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }

    // Test pour vérifier que les dates de début et de fin peuvent être null
    @Test
    void promotion_startDateAndEndDateCanBeNull() {
        // GIVEN : Création d'une instance de Promotion
        Promotion promotion = new Promotion();

        // WHEN : Définition des dates de début et de fin à null
        promotion.setStartDate(null);
        promotion.setEndDate(null);

        // THEN : Vérification que les dates sont bien null
        assertNull(promotion.getStartDate(), "La date de début peut être null.");
        assertNull(promotion.getEndDate(), "La date de fin peut être null.");
    }

    // Test pour vérifier que le pourcentage de réduction est correctement stocké
    @Test
    void promotion_discountPercentageIsStoredCorrectly() {
        // GIVEN : Création d'une instance de Promotion et définition d'un pourcentage
        // de réduction
        Promotion promotion = new Promotion();
        int discount = 50;
        promotion.setDiscountPercentage(discount);

        // THEN : Vérification que le pourcentage de réduction est correctement récupéré
        assertEquals(discount, promotion.getDiscountPercentage(),
                "Le pourcentage de réduction doit être stocké et récupéré correctement.");
    }

    // Test pour vérifier l'état actif de la promotion
    @Test
    void promotion_activeStatusIsStoredCorrectly() {
        // GIVEN : Création d'une instance de Promotion et définition de l'état actif à
        // false
        Promotion promotion = new Promotion();
        promotion.setActive(false);

        // THEN : Vérification que l'état actif est correctement récupéré
        assertFalse(promotion.isActive(), "L'état actif doit être stocké et récupéré correctement (false).");

        // WHEN : Modification de l'état actif à true
        promotion.setActive(true);

        // THEN : Vérification que l'état actif a été correctement modifié
        assertTrue(promotion.isActive(), "L'état actif doit être stocké et récupéré correctement (true).");
    }
}