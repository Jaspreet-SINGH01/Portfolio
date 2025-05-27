package com.videoflix.content.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor // Lombok: génère un constructeur sans arguments
@AllArgsConstructor // Lombok: génère un constructeur avec tous les arguments
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT") // Pour un texte plus long
    private String description;

    private LocalDate releaseDate;

    private Integer durationMinutes; // Durée en minutes

    private String posterUrl; // URL de l'image de la jaquette

    private String trailerUrl; // URL de la bande-annonce

    @Enumerated(EnumType.STRING) // Stocke le nom de l'enum en DB
    @Column(nullable = false)
    private ContentType type; // MOVIE, TV_SERIES, DOCUMENTARY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status; // DRAFT, PUBLISHED, ARCHIVED, UPCOMING

    @Enumerated(EnumType.STRING)
    private Rating rating; // G, PG, R, etc.

    @Enumerated(EnumType.STRING)
    private SubscriptionLevelRequired minSubscriptionLevel; // BASIC, STANDARD, PREMIUM

    @ManyToMany
    @JoinTable(
        name = "content_genres",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    // Vous ajouteriez des relations pour les acteurs, réalisateurs ici aussi

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}