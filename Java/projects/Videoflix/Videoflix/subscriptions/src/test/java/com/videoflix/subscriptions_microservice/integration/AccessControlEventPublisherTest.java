// Package de test d'intégration pour le microservice d'abonnements
package com.videoflix.subscriptions_microservice.integration;

// Imports pour la sérialisation JSON et les outils de test (JUnit, Mockito, etc.)
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Active l'extension Mockito avec JUnit 5 pour gérer les annotations @Mock, @InjectMocks, etc.
@ExtendWith(MockitoExtension.class)
class AccessControlEventPublisherTest {

        // Constantes pour éviter les chaînes en dur dans les tests
        private static final String EXCHANGE = "access.control.events";
        private static final String REACTIVATED_ROUTING_KEY = "subscription.reactivated";
        private static final String CANCELLED_ROUTING_KEY = "subscription.cancelled";

        // Mock du composant RabbitTemplate utilisé pour envoyer les messages vers
        // RabbitMQ
        @Mock
        private RabbitTemplate rabbitTemplate;

        // Mock de l'ObjectMapper pour contrôler la sérialisation JSON dans les tests
        @Mock
        private ObjectMapper objectMapper;

        // Injecte les mocks dans l'instance de la classe testée
        @InjectMocks
        private AccessControlEventPublisher accessControlEventPublisher;

        // Captor pour intercepter le message JSON envoyé à RabbitMQ
        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Teste que l'événement "réactivation d'abonnement" est publié correctement
        @Test
        void publishSubscriptionReactivatedForAccessControl_shouldPublishCorrectEvent() throws Exception {
                Long userId = 123L;
                String subscriptionLevelName = "PREMIUM";

                // Simule une chaîne JSON générée par ObjectMapper
                String expectedMessage = "{\"userId\":123,\"subscriptionLevel\":\"PREMIUM\",\"reactivatedAt\":\"2023-05-05T12:00:00\"}";
                when(objectMapper.writeValueAsString(anyMap())).thenReturn(expectedMessage);

                // Appel de la méthode testée
                accessControlEventPublisher.publishSubscriptionReactivatedForAccessControl(userId,
                                subscriptionLevelName);

                // Vérifie que le message a été envoyé à RabbitMQ avec les bons paramètres
                verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(REACTIVATED_ROUTING_KEY),
                                messageCaptor.capture());
                verify(objectMapper).writeValueAsString(anyMap());

                // Récupère et désérialise le message JSON envoyé
                String actualMessage = messageCaptor.getValue();
                assertNotNull(actualMessage);

                Map<?, ?> payload = new ObjectMapper().readValue(actualMessage, Map.class);

                // Vérifie le contenu du message (note : Jackson convertit les Long en Integer)
                assertEquals(userId.intValue(), payload.get("userId"));
                assertEquals(subscriptionLevelName, payload.get("subscriptionLevel"));
                assertNotNull(payload.get("reactivatedAt"));
        }

        // Teste que l'événement "annulation d'abonnement" est publié correctement
        @Test
        void publishSubscriptionCancelledForAccessControl_shouldPublishCorrectEvent() throws Exception {
                Long userId = 456L;
                String subscriptionLevelName = "BASIC";
                String reason = "User requested cancellation";

                // Simule un message JSON
                String expectedMessage = "{\"userId\":456,\"subscriptionLevel\":\"BASIC\",\"reason\":\"User requested cancellation\",\"cancelledAt\":\"2023-05-05T12:00:00\"}";
                when(objectMapper.writeValueAsString(anyMap())).thenReturn(expectedMessage);

                // Appel de la méthode testée
                accessControlEventPublisher.publishSubscriptionCancelledForAccessControl(userId, subscriptionLevelName,
                                reason);

                // Vérifie que le message a été envoyé à RabbitMQ
                verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(CANCELLED_ROUTING_KEY), messageCaptor.capture());
                verify(objectMapper).writeValueAsString(anyMap());

                // Analyse du message
                String actualMessage = messageCaptor.getValue();
                assertNotNull(actualMessage);

                Map<?, ?> payload = new ObjectMapper().readValue(actualMessage, Map.class);

                // Vérifie le contenu du message
                assertEquals(userId.intValue(), payload.get("userId"));
                assertEquals(subscriptionLevelName, payload.get("subscriptionLevel"));
                assertEquals(reason, payload.get("reason"));
                assertNotNull(payload.get("cancelledAt"));
        }

        // Vérifie que les erreurs de sérialisation JSON sont gérées sans plantage
        @Test
        void publishSubscriptionReactivatedForAccessControl_shouldHandleSerializationException() throws Exception {
                Long userId = 789L;
                String subscriptionLevelName = "ULTRA";

                // Simule une exception lors de la sérialisation
                when(objectMapper.writeValueAsString(anyMap())).thenThrow(new Exception("Serialization error"));

                // Appel de la méthode — l'erreur doit être capturée en interne
                accessControlEventPublisher.publishSubscriptionReactivatedForAccessControl(userId,
                                subscriptionLevelName);

                // On vérifie que malgré l'erreur, un message a tout de même été "envoyé"
                verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(REACTIVATED_ROUTING_KEY), anyString());
                verify(objectMapper).writeValueAsString(anyMap());
        }

        // Vérifie que les erreurs lors de l'envoi à RabbitMQ sont gérées sans plantage
        @Test
        void publishSubscriptionReactivatedForAccessControl_shouldHandleRabbitTemplateException() throws Exception {
                Long userId = 999L;
                String subscriptionLevelName = "STANDARD";
                String serializedPayload = "{\"userId\":999,\"subscriptionLevel\":\"STANDARD\",\"reactivatedAt\":\"...\"}";

                // Simule une sérialisation correcte
                when(objectMapper.writeValueAsString(anyMap())).thenReturn(serializedPayload);

                // Simule une exception lors de l'envoi à RabbitMQ
                doThrow(new RuntimeException("RabbitMQ error"))
                                .when(rabbitTemplate)
                                .convertAndSend(EXCHANGE, REACTIVATED_ROUTING_KEY, serializedPayload);

                // Appel de la méthode — l'erreur doit être capturée en interne
                accessControlEventPublisher.publishSubscriptionReactivatedForAccessControl(userId,
                                subscriptionLevelName);

                // Vérifie que les méthodes ont bien été appelées malgré l'exception
                verify(rabbitTemplate).convertAndSend(EXCHANGE, REACTIVATED_ROUTING_KEY, serializedPayload);
                verify(objectMapper).writeValueAsString(anyMap());
        }
}