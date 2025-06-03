package com.videoflix.content.services;

import com.videoflix.content.entities.Content;
import com.videoflix.content.entities.ContentStatus;
import com.videoflix.content.repositories.CategoryRepository;
import com.videoflix.content.repositories.ContentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;

    public ContentService(ContentRepository contentRepository, CategoryRepository categoryRepository) {
        this.contentRepository = contentRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Content createContent(Content content) {
        // Logique métier avant la sauvegarde (ex: validation plus poussée)
        content.setStatus(ContentStatus.DRAFT); // Définit un statut initial
        return contentRepository.save(content);
    }

    @Transactional(readOnly = true)
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contenu non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Content> getAllPublishedContent() {
        return contentRepository.findByStatus(ContentStatus.PUBLISHED);
    }

    @Transactional
    public Content updateContent(Long id, Content updatedContent) {
        Content existingContent = getContentById(id); // Récupère l'existant ou lance une exception
        // Mettre à jour les propriétés de existingContent avec celles de updatedContent
        existingContent.setTitle(updatedContent.getTitle());
        existingContent.setDescription(updatedContent.getDescription());
        existingContent.setReleaseDate(updatedContent.getReleaseDate());
        existingContent.setDurationMinutes(updatedContent.getDurationMinutes());
        existingContent.setPosterUrl(updatedContent.getPosterUrl());
        existingContent.setTrailerUrl(updatedContent.getTrailerUrl());
        existingContent.setType(updatedContent.getType());
        existingContent.setRating(updatedContent.getRating());
        existingContent.setMinSubscriptionLevel(updatedContent.getMinSubscriptionLevel());
        // Gestion de la mise à jour des genres, acteurs, etc. ici
        return contentRepository.save(existingContent);
    }

    @Transactional
    public void deleteContent(Long id) {
        contentRepository.deleteById(id);
    }

    @Transactional
    public Content publishContent(Long id) {
        Content content = getContentById(id);
        content.setStatus(ContentStatus.PUBLISHED);
        return contentRepository.save(content);
        // Déclencher un événement ici: ContentPublishedEvent
    }

    // Ajouter des méthodes pour les séries/épisodes si vous les gérez
}