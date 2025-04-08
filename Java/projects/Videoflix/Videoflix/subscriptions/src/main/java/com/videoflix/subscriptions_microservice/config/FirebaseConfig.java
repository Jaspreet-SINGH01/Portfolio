package com.videoflix.subscriptions_microservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("classpath:path/to/your/serviceAccountKey.json") // Chemin vers votre fichier de clé de compte de service Firebase
    private Resource serviceAccountKey;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountKey.getInputStream()))
                .build();
        FirebaseApp.initializeApp(options);
        return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
    }
}