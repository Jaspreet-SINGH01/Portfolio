package com.videoflix.subscriptions_microservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FirebaseConfigTest {

    @InjectMocks
    private FirebaseConfig firebaseConfig;

    @Mock
    private Resource serviceAccountKey;

    @Test
    void firebaseMessaging_shouldInitializeFirebaseAppAndReturnMessagingInstance_whenNoAppExists() throws IOException {
        // GIVEN : Aucune instance FirebaseApp n'est initialisée
        try (MockedStatic<FirebaseApp> firebaseAppStatic = mockStatic(FirebaseApp.class)) {
            firebaseAppStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());

            // Mock du Resource pour retourner un InputStream valide
            String fakeKeyContent = "{\"type\": \"service_account\"}";
            InputStream inputStream = new ByteArrayInputStream(fakeKeyContent.getBytes());
            when(serviceAccountKey.getInputStream()).thenReturn(inputStream);

            // Mock de FirebaseOptions builder
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
            try (MockedStatic<FirebaseOptions> firebaseOptionsStatic = mockStatic(FirebaseOptions.class)) {
                firebaseOptionsStatic.when(FirebaseOptions::builder).thenReturn(optionsBuilder);
                when(optionsBuilder.setCredentials(any(GoogleCredentials.class))).thenReturn(optionsBuilder);
                when(optionsBuilder.build()).thenReturn(mock(FirebaseOptions.class));

                // Mock de FirebaseMessaging
                FirebaseMessaging mockFirebaseMessaging = mock(FirebaseMessaging.class);
                try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
                    firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseMessaging);

                    // WHEN : La méthode firebaseMessaging est appelée
                    FirebaseMessaging messaging = firebaseConfig.firebaseMessaging();

                    // THEN : Vérification que FirebaseApp.initializeApp a été appelé et que
                    // l'instance de FirebaseMessaging est retournée
                    firebaseAppStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), times(1));
                    assertEquals(mockFirebaseMessaging, messaging);
                }
            }
        }
    }

    @Test
    void firebaseMessaging_shouldReturnExistingMessagingInstance_whenAppAlreadyExists() throws IOException {
        // GIVEN : Une instance FirebaseApp est déjà initialisée
        FirebaseApp mockFirebaseApp = mock(FirebaseApp.class);
        List<FirebaseApp> existingApps = Collections.singletonList(mockFirebaseApp);

        // Mock de FirebaseMessaging
        FirebaseMessaging mockFirebaseMessaging = mock(FirebaseMessaging.class);

        try (MockedStatic<FirebaseApp> firebaseAppStatic = mockStatic(FirebaseApp.class)) {
            firebaseAppStatic.when(FirebaseApp::getApps).thenReturn(existingApps);

            try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
                firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseMessaging);

                // WHEN : La méthode firebaseMessaging est appelée
                FirebaseMessaging messaging = firebaseConfig.firebaseMessaging();

                // THEN : Vérification que FirebaseApp.initializeApp n'a pas été appelé et que
                // l'instance existante de FirebaseMessaging est retournée
                firebaseAppStatic.verify(FirebaseApp::initializeApp, never());
                assertEquals(mockFirebaseMessaging, messaging);
            }
        }
    }

    @Test
    void firebaseMessaging_shouldThrowIOException_whenServiceAccountKeyFailsToLoad() throws IOException {
        // GIVEN : Le Resource serviceAccountKey lève une IOException lors de la lecture
        // de l'InputStream
        when(serviceAccountKey.getInputStream()).thenThrow(new IOException("Failed to load service account key"));

        // WHEN : Tentative d'appel de la méthode firebaseMessaging
        try {
            firebaseConfig.firebaseMessaging();
            fail("Une IOException aurait dû être levée.");
        } catch (IOException e) {
            // THEN : Vérification que l'IOException a été levée avec le message attendu
            assertEquals("Failed to load service account key", e.getMessage());
            // Vérification que FirebaseApp.initializeApp n'a pas été appelé
            try (MockedStatic<FirebaseApp> firebaseAppStatic = mockStatic(FirebaseApp.class)) {
                firebaseAppStatic.verify(FirebaseApp::initializeApp, never());
            }
        }
    }
}