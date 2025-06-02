package com.videoflix.content.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name = "subscription_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionLevelRequired {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ex: "BASIC"

    @Column(nullable = false)
    private String displayName; // Ex: "Abonnement Basique"

    private String description;
    private Integer priority;

    // Constructeur pour faciliter l'initialisation depuis l'enum interne
    public SubscriptionLevelRequired(SubscriptionLevelRequired.Level level) { // Référence l'enum interne
        this.name = level.name();
        this.displayName = level.getDisplayName();
        this.description = level.getDescription();
        this.priority = level.getPriority();
    }

    // --- Enumération interne au sein de l'entité SubscriptionLevelRequired ---
    @Getter
    public enum Level {
        BASIC("Abonnement Basique", "Accès au contenu standard.", 1),
        STANDARD("Abonnement Standard", "Accès au contenu HD et à certains contenus exclusifs.", 2),
        PREMIUM("Abonnement Premium", "Accès à tout le contenu, y compris 4K et avant-premières.", 3);

        private final String displayName;
        private final String description;
        private final Integer priority;

        Level(String displayName, String description, Integer priority) {
            this.displayName = displayName;
            this.description = description;
            this.priority = priority;
        }

        public static Level fromName(String name) {
            for (Level level : Level.values()) {
                if (level.name().equalsIgnoreCase(name)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Niveau d'abonnement inconnu: " + name);
        }
    }
}