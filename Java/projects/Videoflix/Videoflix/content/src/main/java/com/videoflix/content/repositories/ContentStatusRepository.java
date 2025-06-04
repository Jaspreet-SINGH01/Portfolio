package com.videoflix.content.repositories;

import com.videoflix.content.entities.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentStatusRepository extends JpaRepository<ContentStatus, Long> {

    /**
     * Trouve un statut de contenu par son nom (ex: "DRAFT", "PUBLISHED",
     * "ARCHIVED").
     * Cette méthode sera utilisée pour récupérer l'entité ContentStatus
     * correspondante à l'enum Java.
     * 
     * @param name Le nom du statut à rechercher.
     * @return Un Optional contenant le statut si trouvé, sinon un Optional vide.
     */
    Optional<ContentStatus> findByName(String name);
}