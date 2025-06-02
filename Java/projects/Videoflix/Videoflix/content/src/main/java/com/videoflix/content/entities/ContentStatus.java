package com.videoflix.content.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name = "content_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ex: "DRAFT", "PUBLISHED", "ARCHIVED"

    @Column(nullable = false)
    private String displayName; // Ex: "Brouillon", "Publié"

    private String description;

    public ContentStatus(ContentStatus.Status status) {
        this.name = status.name();
        this.displayName = status.getDisplayName();
        this.description = status.getDescription();
    }

    @Getter
    public enum Status {
        DRAFT("Brouillon", "Contenu en cours de création, non visible publiquement."),
        PUBLISHED("Publié", "Contenu visible et disponible pour le visionnage."),
        ARCHIVED("Archivé", "Contenu anciennement publié mais retiré, non visible publiquement."),
        UPCOMING("Prochainement", "Contenu annoncé, non encore disponible pour le visionnage.");

        private final String displayName;
        private final String description;

        Status(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public static Status fromName(String name) {
            for (Status status : Status.values()) {
                if (status.name().equalsIgnoreCase(name)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Statut de contenu inconnu: " + name);
        }
    }
}