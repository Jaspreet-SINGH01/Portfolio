package com.videoflix.content.repositories;

import com.videoflix.content.entities.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentTypeRepository extends JpaRepository<ContentType, Long> {

    /**
     * Trouve un ContentType par son nom (ex: "MOVIE", "TV_SERIES").
     * Cette méthode sera utilisée pour récupérer l'entité ContentType
     * correspondante à l'enum Java.
     * 
     * @param name Le nom du ContentType à rechercher.
     * @return Un Optional contenant le ContentType si trouvé, sinon un Optional
     *         vide.
     */
    Optional<ContentType> findByName(String name);
}