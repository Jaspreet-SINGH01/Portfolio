package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.Level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionCancelledEventPublisherTest {

        @Mock
        private RabbitTemplate rabbitTemplate;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private SubscriptionCancelledEventPublisher subscriptionCancelledEventPublisher;

        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement d'annulation
        // d'abonnement avec toutes les informations
        @Test
        void publishSubscriptionCancelledEvent_shouldPublishCorrectlyWithAllInfo() throws Exception {
                // GIVEN : Un abonnement avec un utilisateur et un niveau d'abonnement, et une
                // raison d'annulation
                User user = new User();
                user.setId(123L);
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setId(456L);
                subscriptionLevel.setLevel(Level.BASIC);
                Subscription subscription = new Subscription();
                subscription.setId(789L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(subscriptionLevel);
                String cancellationReason = "User request";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 789L,
                                "userId", 123L,
                                "cancellationDate", LocalDateTime.now().toString(),
                                "cancellationReason", cancellationReason,
                                "subscriptionLevelId", 456L,
                                "subscriptionLevelName", "PREMIUM");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription, cancellationReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.cancelled",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la publication correcte d'un événement d'annulation
        // d'abonnement sans utilisateur
        @Test
        void publishSubscriptionCancelledEvent_shouldPublishCorrectlyWithoutUser() throws Exception {
                // GIVEN : Un abonnement sans utilisateur et une raison d'annulation
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setId(101L);
                subscriptionLevel.setLevel(Level.PREMIUM);
                Subscription subscription = new Subscription();
                subscription.setId(202L);
                subscription.setUser(null);
                subscription.setSubscriptionLevel(subscriptionLevel);
                String cancellationReason = "Payment failure";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 202L,
                                "userId", null,
                                "cancellationDate", LocalDateTime.now().toString(),
                                "cancellationReason", cancellationReason,
                                "subscriptionLevelId", 101L,
                                "subscriptionLevelName", "BASIC");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription, cancellationReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.cancelled",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la publication correcte d'un événement d'annulation
        // d'abonnement sans niveau d'abonnement
        @Test
        void publishSubscriptionCancelledEvent_shouldPublishCorrectlyWithoutSubscriptionLevel() throws Exception {
                // GIVEN : Un abonnement sans niveau d'abonnement et une raison d'annulation
                User user = new User();
                user.setId(303L);
                Subscription subscription = new Subscription();
                subscription.setId(404L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(null);
                String cancellationReason = "Admin intervention";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 404L,
                                "userId", 303L,
                                "cancellationDate", LocalDateTime.now().toString(),
                                "cancellationReason", cancellationReason,
                                "subscriptionLevelId", null,
                                "subscriptionLevelName", null);
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription, cancellationReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.cancelled",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation de
        // l'événement et log de l'erreur
        @Test
        void publishSubscriptionCancelledEvent_shouldHandleSerializationException_andLogError() throws Exception {
                // GIVEN : Un abonnement et une raison d'annulation
                Subscription subscription = new Subscription();
                subscription.setId(505L);
                String cancellationReason = "Serialization error";

                // AND : Configuration de l'ObjectMapper pour lancer une exception lors de la
                // sérialisation
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);

                // AND : Mock du LoggerFactory pour qu'il retourne notre mock de Logger
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionCancelledEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription,
                                        cancellationReason);

                        // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                        // appelée une fois (même si le message sera potentiellement null)
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.cancelled",
                                        (String) null);

                        // THEN : Vérification que l'ObjectMapper a tenté de sérialiser le payload
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur avec le message et
                        // l'exception
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'Évènement SubscriptionCancelledEvent : {}"),
                                        eq(errorMessage),
                                        any(Exception.class));
                }
        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi du message via
        // RabbitTemplate et log de l'erreur
        @Test
        void publishSubscriptionCancelledEvent_shouldHandleRabbitTemplateException_andLogError() throws Exception {
                // GIVEN : Un abonnement et une raison d'annulation
                Subscription subscription = new Subscription();
                subscription.setId(606L);
                String cancellationReason = "RabbitMQ error";
                Map<String, Object> payload = Map.of(
                                "subscriptionId", 606L,
                                "userId", null,
                                "cancellationDate", LocalDateTime.now().toString(),
                                "cancellationReason", cancellationReason,
                                "subscriptionLevelId", null,
                                "subscriptionLevelName", null);
                String expectedMessage = new ObjectMapper().writeValueAsString(payload);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // AND : Configuration du RabbitTemplate pour lancer une exception lors de
                // l'envoi
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                "subscription.events",
                                                "subscription.cancelled",
                                                expectedMessage);

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);

                // AND : Mock du LoggerFactory
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionCancelledEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionCancelledEventPublisher.publishSubscriptionCancelledEvent(subscription,
                                        cancellationReason);

                        // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                        // appelée une fois
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.cancelled",
                                        expectedMessage);

                        // THEN : Vérification que l'ObjectMapper a tenté de sérialiser le payload
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'Évènement SubscriptionCancelledEvent : {}"),
                                        eq(errorMessage),
                                        any(RuntimeException.class));
                }
        }
}