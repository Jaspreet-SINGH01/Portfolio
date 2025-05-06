package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Promotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour le PromotionRepository
 */
@DataJpaTest
class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Méthode utilitaire pour créer et persister une entité Promotion pour les
     * tests.
     * 
     * @param code               Le code de la promotion
     * @param description        La description de la promotion
     * @param discountPercentage Le pourcentage de réduction
     * @param startDate          La date de début de la promotion
     * @param endDate            La date de fin de la promotion
     * @return L'entité Promotion persistée
     */
    private Promotion createAndPersistPromotion(String code, String description, int discountPercentage,
            LocalDateTime startDate, LocalDateTime endDate) {
        Promotion promotion = new Promotion();
        promotion.setPromotionCode(code);
        promotion.setDescription(description);
        promotion.setDiscountPercentage(discountPercentage);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        return entityManager.persistAndFlush(promotion);
    }

    @Test
    void findByPromotionCode_shouldReturnPromotionForGivenCode() {
        // GIVEN : Création et persistence d'une promotion avec un code spécifique
        String promotionCode = "VDFLX10";
        String description = "Videoflix 10% off";
        int discountPercentage = 10;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(3);

        Promotion persistedPromotion = createAndPersistPromotion(
                promotionCode, description, discountPercentage, startDate, endDate);

        // WHEN : Recherche de la promotion par son code
        Promotion foundPromotion = promotionRepository.findByPromotionCode(promotionCode);

        // THEN : Vérification que la promotion trouvée correspond à celle qui a été
        // persistée
        assertThat(foundPromotion).isNotNull();
        assertThat(foundPromotion.getId()).isEqualTo(persistedPromotion.getId());
        assertThat(foundPromotion.getPromotionCode()).isEqualTo(promotionCode);
        assertThat(foundPromotion.getDescription()).isEqualTo(description);
        assertThat(foundPromotion.getDiscountPercentage()).isEqualTo(discountPercentage);
        assertThat(foundPromotion.getStartDate()).isEqualToIgnoringNanos(startDate);
        assertThat(foundPromotion.getEndDate()).isEqualToIgnoringNanos(endDate);
    }

    @Test
    void findByPromotionCode_shouldReturnNullIfPromotionNotFound() {
        // GIVEN : Aucune promotion avec le code recherché n'est persistée
        String nonExistentCode = "NONEXISTENT";

        // WHEN : Recherche d'une promotion par un code qui n'existe pas dans la base de
        // données
        Promotion foundPromotion = promotionRepository.findByPromotionCode(nonExistentCode);

        // THEN : Vérification que la méthode retourne null
        assertThat(foundPromotion).isNull();
    }

    @Test
    void findActivePromotions_shouldReturnPromotionsValidForCurrentDate() {
        // GIVEN : Création de promotions avec différentes dates de validité
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastStart = now.minusDays(10);
        LocalDateTime futureEnd = now.plusDays(10);
        LocalDateTime pastEnd = now.minusDays(5);
        LocalDateTime futureStart = now.plusDays(5);

        // Promotion active (déjà commencée et pas encore terminée)
        createAndPersistPromotion("ACTIVE", "Active Promotion", 15, pastStart, futureEnd);

        // Promotion expirée
        createAndPersistPromotion("EXPIRED", "Expired Promotion", 20, pastStart, pastEnd);

        // Promotion future
        createAndPersistPromotion("FUTURE", "Future Promotion", 25, futureStart, futureEnd);

        // WHEN : Récupération des promotions actives
        @SuppressWarnings("unchecked")
        Iterable<Promotion> activePromotions = (Iterable<Promotion>) promotionRepository.findByPromotionCode(null);

        // THEN : Vérification que seule la promotion active est retournée
        assertThat(activePromotions).hasSize(1);
        assertThat(activePromotions).extracting(Promotion::getPromotionCode)
                .containsExactly("ACTIVE");
    }
}