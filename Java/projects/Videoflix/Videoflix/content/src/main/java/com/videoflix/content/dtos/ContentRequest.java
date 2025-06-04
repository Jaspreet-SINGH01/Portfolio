package com.videoflix.content.dtos;

import com.videoflix.content.entities.ContentType;
import com.videoflix.content.entities.Rating;
import com.videoflix.content.entities.SubscriptionLevelRequired;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class ContentRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    private String description;
    private LocalDate releaseDate;

    @Positive(message = "La durée doit être positive")
    private Integer durationMinutes;
    private String posterUrl;
    private String trailerUrl;

    @NotNull(message = "Le type de contenu est obligatoire")
    private ContentType type;
    private Rating rating;
    private SubscriptionLevelRequired minSubscriptionLevel;
    private Set<Long> categoryId; // Pour associer des genres existants
    // Ajouter les IDs d'acteurs/réalisateurs
}