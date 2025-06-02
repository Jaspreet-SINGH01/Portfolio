package com.videoflix.content.config;

import com.videoflix.content.entities.ContentType;
import com.videoflix.content.entities.Rating;
import com.videoflix.content.entities.SubscriptionLevelRequired;
import com.videoflix.content.repositories.ContentTypeRepository;
import com.videoflix.content.repositories.RatingRepository;
import com.videoflix.content.repositories.SubscriptionLevelRequiredRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ContentTypeRepository contentTypeRepository,
            RatingRepository ratingRepository,
            SubscriptionLevelRequiredRepository subscriptionLevelRequiredRepository) {
        return _ -> {
            // Content Types
            for (ContentType.Type typeEnum : ContentType.Type.values()) {
                if (contentTypeRepository.findByName(typeEnum.name()).isEmpty()) {
                    contentTypeRepository.save(new ContentType(typeEnum)); // Utilise le constructeur de l'entité
                }
            }

            // Ratings
            for (Rating.Level ratingEnum : Rating.Level.values()) {
                if (ratingRepository.findByName(ratingEnum.name()).isEmpty()) {
                    ratingRepository.save(new Rating(ratingEnum)); // Utilise le constructeur de l'entité
                }
            }

            // Subscription Levels
            for (SubscriptionLevelRequired.Level levelEnum : SubscriptionLevelRequired.Level.values()) {
                if (subscriptionLevelRequiredRepository.findByName(levelEnum.name()).isEmpty()) {
                    subscriptionLevelRequiredRepository.save(new SubscriptionLevelRequired(levelEnum)); // Utilise le
                                                                                                        // constructeur
                                                                                                        // de l'entité
                }
            }
        };
    }
}