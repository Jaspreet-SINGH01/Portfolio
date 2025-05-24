package com.videoflix.subscriptions_microservice.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet l'initialisation des mocks par Mockito.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour WelcomeEmailConsumer")
class WelcomeEmailConsumerTest {

    // @Mock pour le service de notification, nous vérifierons ses appels.
    @Mock
    private NotificationService notificationService;

    // @Mock pour ObjectMapper, qui est utilisé pour désérialiser le message.
    @Mock
    private ObjectMapper objectMapper;

    // @InjectMocks crée une instance de WelcomeEmailConsumer et y injecte les
    // mocks.
    @InjectMocks
    private WelcomeEmailConsumer welcomeEmailConsumer;

    // Déclaration des objets User et Subscription pour les tests.
    private User testUser;
    private Subscription testSubscription;

    /**
     * Méthode exécutée avant chaque test.
     * Initialise les objets de test et réinitialise les mocks.
     */
    @BeforeEach
    void setUp() {
        // Crée un utilisateur de test.
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstname("John");

        // Crée un abonnement de test.
        testSubscription = new Subscription();
        testSubscription.setId(101L);
        // Assurez-vous que l'abonnement a un utilisateur associé si nécessaire.
        testSubscription.setUser(testUser);

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(notificationService, objectMapper);
    }

    @Test
    @DisplayName("Devrait traiter un message valide et envoyer un e-mail de bienvenue")
    void handleNewSubscriptionCreatedEvent_shouldProcessValidMessageAndSendWelcomeEmail() throws IOException {
        // GIVEN: Un message JSON valide simulant un NewSubscriptionCreatedEvent.
        String validMessage = "{\"user\":{\"id\":1,\"email\":\"test@example.com\",\"firstname\":\"John\"},\"subscription\":{\"id\":101}}";

        // Crée l'événement que `objectMapper.readValue` est censé retourner.
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(validMessage, testUser, testSubscription);

        // Configure le comportement des mocks:
        // 1. Lorsque `objectMapper.readValue` est appelé avec le message et le type
        // d'événement,
        // il doit retourner notre événement de test.
        when(objectMapper.readValue(validMessage, NewSubscriptionCreatedEvent.class))
                .thenReturn(event);
        // 2. Lorsque `notificationService.sendWelcomeEmail` est appelé, il ne fait rien
        // (méthode void).
        doNothing().when(notificationService).sendWelcomeEmail(testUser, testSubscription);

        // WHEN: Le consommateur reçoit le message.
        welcomeEmailConsumer.handleNewSubscriptionCreatedEvent(validMessage);

        // THEN:
        // 1. Vérifie que `objectMapper.readValue` a été appelé une fois avec les bons
        // arguments.
        verify(objectMapper, times(1)).readValue(validMessage, NewSubscriptionCreatedEvent.class);
        // 2. Vérifie que `notificationService.sendWelcomeEmail` a été appelé une fois
        // avec le bon utilisateur et abonnement.
        verify(notificationService, times(1)).sendWelcomeEmail(testUser, testSubscription);
        // 3. Vérifie qu'aucune autre interaction n'a eu lieu avec les mocks (par
        // exemple, des gestions d'erreur).
        verifyNoMoreInteractions(notificationService); // Ne vérifie pas objectMapper car il est utilisé avant
    }

    @Test
    @DisplayName("Devrait gérer une IOException lors de la désérialisation du message")
    void handleNewSubscriptionCreatedEvent_shouldHandleIOExceptionOnDeserialization() throws IOException {
        // GIVEN: Un message JSON invalide qui causera une IOException.
        String invalidMessage = "{invalid json";

        // Configure le comportement du mock `objectMapper` pour lancer une
        // `IOException`.
        when(objectMapper.readValue(invalidMessage, NewSubscriptionCreatedEvent.class))
                .thenThrow(new IOException("Erreur de format JSON simulée"));

        // WHEN: Le consommateur reçoit le message invalide.
        welcomeEmailConsumer.handleNewSubscriptionCreatedEvent(invalidMessage);

        // THEN:
        // 1. Vérifie que `objectMapper.readValue` a été appelé.
        verify(objectMapper, times(1)).readValue(invalidMessage, NewSubscriptionCreatedEvent.class);
        // 2. Vérifie que `notificationService.sendWelcomeEmail` n'a JAMAIS été appelé
        // (car la désérialisation a échoué).
        verify(notificationService, never()).sendWelcomeEmail(testUser, testSubscription);
        // (On pourrait aussi vérifier les logs ici pour confirmer l'enregistrement de
        // l'erreur de désérialisation.)
    }

    @Test
    @DisplayName("Devrait gérer une exception lors de l'envoi de l'e-mail de bienvenue")
    void handleNewSubscriptionCreatedEvent_shouldHandleExceptionOnSendingEmail() throws IOException {
        // GIVEN: Un message JSON valide, mais `notificationService.sendWelcomeEmail`
        // lève une exception.
        String validMessage = "{\"user\":{\"id\":1,\"email\":\"test@example.com\",\"firstname\":\"John\"},\"subscription\":{\"id\":101}}";
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(validMessage, testUser, testSubscription);

        // Configure `objectMapper` pour désérialiser avec succès.
        when(objectMapper.readValue(validMessage, NewSubscriptionCreatedEvent.class))
                .thenReturn(event);
        // Configure `notificationService.sendWelcomeEmail` pour lancer une
        // `RuntimeException`.
        doThrow(new RuntimeException("Erreur d'envoi d'e-mail simulée"))
                .when(notificationService).sendWelcomeEmail(testUser, testSubscription);

        // WHEN: Le consommateur reçoit le message.
        welcomeEmailConsumer.handleNewSubscriptionCreatedEvent(validMessage);

        // THEN:
        // 1. Vérifie que `objectMapper.readValue` a été appelé.
        verify(objectMapper, times(1)).readValue(validMessage, NewSubscriptionCreatedEvent.class);
        // 2. Vérifie que `notificationService.sendWelcomeEmail` a été appelé (et a
        // échoué comme simulé).
        verify(notificationService, times(1)).sendWelcomeEmail(testUser, testSubscription);
        // (On pourrait aussi vérifier les logs ici pour confirmer l'enregistrement de
        // l'erreur d'envoi d'e-mail.)
    }

    @Test
    @DisplayName("Devrait gérer un message vide")
    void handleNewSubscriptionCreatedEvent_shouldHandleEmptyMessage() throws IOException {
        // GIVEN: Un message vide.
        String emptyMessage = "";

        // Configure `objectMapper` pour lancer une `IOException` car un message vide
        // n'est pas un JSON valide.
        when(objectMapper.readValue(emptyMessage, NewSubscriptionCreatedEvent.class))
                .thenThrow(new IOException("Unexpected end-of-input: expected JSON value"));

        // WHEN: Le consommateur reçoit le message vide.
        welcomeEmailConsumer.handleNewSubscriptionCreatedEvent(emptyMessage);

        // THEN:
        // 1. Vérifie que `objectMapper.readValue` a été appelé.
        verify(objectMapper, times(1)).readValue(emptyMessage, NewSubscriptionCreatedEvent.class);
        // 2. Vérifie que `notificationService.sendWelcomeEmail` n'a PAS été appelé.
        verify(notificationService, never()).sendWelcomeEmail(any(User.class), any(Subscription.class));
    }
}