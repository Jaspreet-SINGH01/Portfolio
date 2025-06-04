package com.videoflix.content.services;

import com.videoflix.content.entities.Content;
import com.videoflix.content.entities.Category;
import com.videoflix.content.entities.ContentType;
import com.videoflix.content.entities.Rating;
import com.videoflix.content.entities.SubscriptionLevelRequired;
import com.videoflix.content.entities.ContentStatus;
import com.videoflix.content.repositories.ContentRepository;
import com.videoflix.content.repositories.ContentTypeRepository;
import com.videoflix.content.repositories.RatingRepository;
import com.videoflix.content.repositories.SubscriptionLevelRequiredRepository;
import com.videoflix.content.repositories.ContentStatusRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final CategoryService categoryService;
    private final ContentTypeRepository contentTypeRepository;
    private final RatingRepository ratingRepository;
    private final SubscriptionLevelRequiredRepository subscriptionLevelRequiredRepository;
    private final ContentStatusRepository contentStatusRepository;
    private ContentService self;

    public ContentService(ContentRepository contentRepository,
            CategoryService categoryService,
            ContentTypeRepository contentTypeRepository,
            RatingRepository ratingRepository,
            SubscriptionLevelRequiredRepository subscriptionLevelRequiredRepository,
            ContentStatusRepository contentStatusRepository,
            @Lazy ContentService self) {
        this.contentRepository = contentRepository;
        this.categoryService = categoryService;
        this.contentTypeRepository = contentTypeRepository;
        this.ratingRepository = ratingRepository;
        this.subscriptionLevelRequiredRepository = subscriptionLevelRequiredRepository;
        this.contentStatusRepository = contentStatusRepository;
        this.self = self;
    }

    @Transactional
    public Content createContent(Content content,
            ContentType.Type typeEnum,
            Rating.Level ratingEnum,
            SubscriptionLevelRequired.Level subscriptionLevelEnum,
            ContentStatus.Status initialStatusEnum,
            Set<Long> categoryId) {
        ContentType contentType = contentTypeRepository.findByName(typeEnum.name())
                .orElseThrow(() -> new EntityNotFoundException("ContentType non trouvé: " + typeEnum.name()));
        content.setType(contentType);

        if (ratingEnum != null) {
            Rating rating = ratingRepository.findByName(ratingEnum.name())
                    .orElseThrow(() -> new EntityNotFoundException("Rating non trouvé: " + ratingEnum.name()));
            content.setRating(rating);
        }

        if (subscriptionLevelEnum != null) {
            SubscriptionLevelRequired level = subscriptionLevelRequiredRepository
                    .findByName(subscriptionLevelEnum.name())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "SubscriptionLevel non trouvé: " + subscriptionLevelEnum.name()));
            content.setMinSubscriptionLevel(level);
        }

        // Définir le statut initial
        ContentStatus initialStatus = contentStatusRepository.findByName(initialStatusEnum.name())
                .orElseThrow(
                        () -> new EntityNotFoundException("ContentStatus non trouvé: " + initialStatusEnum.name()));
        content.setStatus(initialStatus);

        // Associer les catégories
        if (categoryId != null && !categoryId.isEmpty()) {
            Set<Category> categories = categoryId.stream()
                    .map(categoryService::getCategoryById) // Utilisez categoryService pour récupérer les catégories
                    .collect(Collectors.toSet());
            content.setCategories(categories); // Assurez-vous que l'entité Content a bien une méthode setCategories
        }

        return contentRepository.save(content);
    }

    /**
     * Récupère un contenu par son ID.
     * 
     * @param id L'ID du contenu à récupérer.
     * @return Le contenu correspondant.
     * @throws EntityNotFoundException si aucun contenu n'est trouvé avec cet ID.
     */
    @Transactional(readOnly = true)
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contenu non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Content> getAllPublishedContent() {
        ContentStatus publishedStatus = contentStatusRepository.findByName(ContentStatus.Status.PUBLISHED.name())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Le statut 'PUBLISHED' n'est pas défini dans la base de données."));
        return contentRepository.findByStatus(publishedStatus); // findByStatus doit accepter ContentStatus
    }

    @Transactional
    public Content updateContent(Long id,
            Content updatedContent,
            ContentType.Type typeEnum,
            Rating.Level ratingEnum,
            SubscriptionLevelRequired.Level subscriptionLevelEnum,
            ContentStatus.Status statusEnum,
            Set<Long> categoryId) {
        Content existingContent = self.getContentById(id);
        existingContent.setTitle(updatedContent.getTitle());
        existingContent.setDescription(updatedContent.getDescription());
        existingContent.setReleaseDate(updatedContent.getReleaseDate());
        existingContent.setDurationMinutes(updatedContent.getDurationMinutes());
        existingContent.setPosterUrl(updatedContent.getPosterUrl());
        existingContent.setTrailerUrl(updatedContent.getTrailerUrl());

        // Mettre à jour les entités de référence
        ContentType contentType = contentTypeRepository.findByName(typeEnum.name())
                .orElseThrow(() -> new EntityNotFoundException("ContentType non trouvé: " + typeEnum.name()));
        existingContent.setType(contentType);

        if (ratingEnum != null) {
            Rating rating = ratingRepository.findByName(ratingEnum.name())
                    .orElseThrow(() -> new EntityNotFoundException("Rating non trouvé: " + ratingEnum.name()));
            existingContent.setRating(rating);
        } else {
            existingContent.setRating(null); // Gérer le cas où le rating est retiré
        }

        if (subscriptionLevelEnum != null) {
            SubscriptionLevelRequired level = subscriptionLevelRequiredRepository
                    .findByName(subscriptionLevelEnum.name())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "SubscriptionLevel non trouvé: " + subscriptionLevelEnum.name()));
            existingContent.setMinSubscriptionLevel(level);
        } else {
            existingContent.setMinSubscriptionLevel(null); // Gérer le cas où le niveau d'abonnement est retiré
        }

        ContentStatus newStatus = contentStatusRepository.findByName(statusEnum.name())
                .orElseThrow(() -> new EntityNotFoundException("ContentStatus non trouvé: " + statusEnum.name()));
        existingContent.setStatus(newStatus);

        // Gérer la mise à jour des catégories (ManyToMany)
        if (categoryId != null) {
            Set<Category> categories = categoryId.stream()
                    .map(categoryService::getCategoryById)
                    .collect(Collectors.toSet());
            existingContent.setCategories(categories);
        } else {
            existingContent.setCategories(new java.util.HashSet<>()); // Ou null, selon la logique
        }

        return contentRepository.save(existingContent);
    }

    @Transactional
    public void deleteContent(Long id) {
        contentRepository.deleteById(id);
    }

    @Transactional
    public Content publishContent(Long id) {
        Content content = self.getContentById(id);
        ContentStatus publishedStatus = contentStatusRepository.findByName(ContentStatus.Status.PUBLISHED.name())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Le statut 'PUBLISHED' n'est pas défini dans la base de données."));
        content.setStatus(publishedStatus);
        return contentRepository.save(content);
        // Déclencher un événement ici: ContentPublishedEvent
    }

    // Ajouter des méthodes pour les séries/épisodes si vous les gérez
}