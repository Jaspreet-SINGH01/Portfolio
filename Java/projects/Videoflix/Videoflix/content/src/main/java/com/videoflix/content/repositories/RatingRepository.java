package com.videoflix.content.repositories;

import com.videoflix.content.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Trouve une classification (Rating) par son nom (ex: "G", "PG-13", "R").
     * Cette méthode sera utilisée pour récupérer l'entité Rating correspondante à
     * l'enum Java.
     * 
     * @param name Le nom de la classification à rechercher.
     * @return Un Optional contenant la classification si trouvée, sinon un Optional
     *         vide.
     */
    Optional<Rating> findByName(String name);
}