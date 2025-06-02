package com.videoflix.content.repositories;

import com.videoflix.content.entities.SubscriptionLevelRequired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionLevelRequiredRepository extends JpaRepository<SubscriptionLevelRequired, Long> {

    /**
     * Trouve un niveau d'abonnement requis par son nom (ex: "BASIC", "PREMIUM").
     * Cette méthode est utilisée pour récupérer l'entité SubscriptionLevelRequired
     * correspondante à l'enum Java.
     * 
     * @param name Le nom du niveau d'abonnement à rechercher.
     * @return Un Optional contenant le niveau d'abonnement si trouvé, sinon un
     *         Optional vide.
     */
    Optional<SubscriptionLevelRequired> findByName(String name);
}