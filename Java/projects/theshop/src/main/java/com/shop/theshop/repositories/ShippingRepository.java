package com.shop.theshop.repositories;

import com.shop.theshop.entities.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    // Vous pouvez ajouter des méthodes de requête personnalisées ici si nécessaire
}

