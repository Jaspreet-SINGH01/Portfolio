package com.videoflix.content.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name = "content_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ex: "MOVIE", "TV_SERIES"

    @Column(nullable = false)
    private String displayName; // Ex: "Film", "Série TV"

    private String description;

    public ContentType(ContentType.Type type) { // Référence l'enum interne
        this.name = type.name();
        this.displayName = type.getDisplayName();
        this.description = type.getDescription();
    }

    @Getter
    public enum Type {
        MOVIE("Film", "Un long-métrage cinématographique."),
        TV_SERIES("Série TV", "Une série d'épisodes télévisés."),
        DOCUMENTARY("Documentaire", "Film ou émission basée sur des faits réels.");

        private final String displayName;
        private final String description;

        Type(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public static Type fromName(String name) {
            for (Type type : Type.values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Type de contenu inconnu: " + name);
        }
    }
}