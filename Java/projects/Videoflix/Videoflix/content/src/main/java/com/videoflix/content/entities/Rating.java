package com.videoflix.content.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ex: "G", "PG"

    @Column(nullable = false)
    private String displayName; // Ex: "Général Public"

    private String description;
    private Integer minAge;

    public Rating(Rating.Level level) {
        this.name = level.name();
        this.displayName = level.getDisplayName();
        this.description = level.getDescription();
        this.minAge = level.getMinAge();
    }

    @Getter
    public enum Level {
        G("Général Public", "Convient à tous les âges.", 0),
        PG("Accompagnement parental souhaitable", "Certaines scènes peuvent ne pas convenir à de jeunes enfants.", 8),
        PG_13("Accord parental fortement souhaitable", "Le contenu peut ne pas convenir aux moins de 13 ans.", 13),
        R("Interdit aux moins de 17 ans non accompagnés", "Contenu réservé aux adultes ou accompagné d'un adulte.", 17),
        NC_17("Interdit aux moins de 18 ans", "Contenu strictement réservé aux adultes.", 18);

        private final String displayName;
        private final String description;
        private final Integer minAge;

        Level(String displayName, String description, Integer minAge) {
            this.displayName = displayName;
            this.description = description;
            this.minAge = minAge;
        }

        public static Level fromName(String name) {
            for (Level level : Level.values()) {
                if (level.name().equalsIgnoreCase(name)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Classification inconnue: " + name);
        }
    }
}