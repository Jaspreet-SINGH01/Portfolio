package com.videoflix.content.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.videoflix.content.entities.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Méthodes existantes
    Optional<Category> findByName(String name);

    // Nouvelles méthodes optimisées

    /**
     * Recherche insensible à la casse par nom
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Vérifie l'existence par nom (insensible à la casse)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Récupère toutes les catégories triées par nom
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Recherche par nom contenant une chaîne (pour la recherche)
     */
    List<Category> findByNameContainingIgnoreCase(String nameFragment);

    /**
     * Recherche avec JPQL personnalisée pour des cas complexes
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Category> searchCategoriesByNameOrDescription(@Param("search") String search);
}