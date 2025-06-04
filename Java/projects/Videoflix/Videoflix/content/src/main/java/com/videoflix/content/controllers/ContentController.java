package com.videoflix.content.controllers;

import com.videoflix.content.dtos.ContentRequest;
import com.videoflix.content.dtos.ContentResponse;
import com.videoflix.content.dtos.CategoryResponse;
import com.videoflix.content.entities.Content;
import com.videoflix.content.entities.Category;
import com.videoflix.content.services.ContentService;
import com.videoflix.content.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    private final ContentService contentService;
    private final CategoryService categoryService;

    public ContentController(ContentService contentService, CategoryService categoryService) {
        this.contentService = contentService;
        this.categoryService = categoryService;
    }

    // Méthode utilitaire pour convertir Entité -> DTO
    private ContentResponse toContentResponse(Content content) {
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setReleaseDate(content.getReleaseDate());
        response.setDurationMinutes(content.getDurationMinutes());
        response.setPosterUrl(content.getPosterUrl());
        response.setTrailerUrl(content.getTrailerUrl());
        response.setType(content.getType());
        response.setStatus(content.getStatus());
        response.setRating(content.getRating());
        response.setMinSubscriptionLevel(content.getMinSubscriptionLevel());
        // Mapper les catégories
        if (content.getCategories() != null) {
            response.setCategoryResponses(content.getCategories().stream()
                    .map(category -> new CategoryResponse(category.getId(), category.getName()))
                    .collect(Collectors.toSet()));
        }
        response.setCreatedAt(content.getCreatedAt());
        response.setUpdatedAt(content.getUpdatedAt());
        return response;
    }

    // Méthode utilitaire pour convertir DTO -> Entité (pour la création)
    private Content toContentEntity(ContentRequest request) {
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setReleaseDate(request.getReleaseDate());
        content.setDurationMinutes(request.getDurationMinutes());
        content.setPosterUrl(request.getPosterUrl());
        content.setTrailerUrl(request.getTrailerUrl());
        content.setType(request.getType());
        content.setRating(request.getRating());
        content.setMinSubscriptionLevel(request.getMinSubscriptionLevel());
        // Note: Le statut sera défini par le service ou par défaut
        return content;
    }

    @PostMapping
    public ResponseEntity<ContentResponse> createContent(@Valid @RequestBody ContentRequest request) {
        Content content = toContentEntity(request);
        // Associer les catégories
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            Set<Category> categories = request.getCategoryId().stream()
                    .map(categoryService::getCategoryById)
                    .collect(Collectors.toSet());
            content.setCategories(categories);
        }
        Content createdContent = contentService.createContent(content, null, null, null, null, null);
        return new ResponseEntity<>(toContentResponse(createdContent), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getContentById(@PathVariable Long id) {
        Content content = contentService.getContentById(id);
        return ResponseEntity.ok(toContentResponse(content));
    }

    @GetMapping("/published")
    public ResponseEntity<List<ContentResponse>> getAllPublishedContent() {
        List<Content> contents = contentService.getAllPublishedContent();
        List<ContentResponse> responses = contents.stream()
                .map(this::toContentResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentResponse> updateContent(@PathVariable Long id,
            @Valid @RequestBody ContentRequest request) {
        Content updatedContent = toContentEntity(request);
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            Set<Category> categories = request.getCategoryId().stream()
                    .map(categoryService::getCategoryById)
                    .collect(Collectors.toSet());
            updatedContent.setCategories(categories);
        }
        Content content = contentService.updateContent(id, updatedContent, null, null, null, null, null);
        return ResponseEntity.ok(toContentResponse(content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ContentResponse> publishContent(@PathVariable Long id) {
        Content publishedContent = contentService.publishContent(id);
        return ResponseEntity.ok(toContentResponse(publishedContent));
    }

    // Ajouter des endpoints pour les séries/épisodes, recherche, filtrage, etc.
}