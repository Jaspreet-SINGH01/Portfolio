package com.videoflix.subscriptions_microservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("classpath:path/to/your/serviceAccountKey.json") // Chemin vers fichier de clé de compte de service Firebase
    private Resource serviceAccountKey;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Vérifier si une instance FirebaseApp existe déjà
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountKey.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
        }
        return FirebaseMessaging.getInstance();
    }
}