package com.videoflix.content.dtos;

import com.videoflix.content.entities.ContentType;
import com.videoflix.content.entities.ContentStatus;
import com.videoflix.content.entities.Rating;
import com.videoflix.content.entities.SubscriptionLevelRequired;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ContentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private Integer durationMinutes;
    private String posterUrl;
    private String trailerUrl;
    private ContentType type;
    private ContentStatus status;
    private Rating rating;
    private SubscriptionLevelRequired minSubscriptionLevel;
    private Set<CategoryResponse> categoryResponses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}