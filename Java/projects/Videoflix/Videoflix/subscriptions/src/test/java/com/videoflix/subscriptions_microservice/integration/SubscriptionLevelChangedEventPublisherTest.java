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
class SubscriptionLevelChangedEventPublisherTest {

        @Mock
        private RabbitTemplate rabbitTemplate;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private SubscriptionLevelChangedEventPublisher subscriptionLevelChangedEventPublisher;

        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement de changement de
        // niveau d'abonnement avec toutes les informations
        @Test
        void publishSubscriptionLevelChangedEvent_shouldPublishCorrectlyWithAllInfo() throws Exception {
                // GIVEN : Un abonnement avec un utilisateur et un nouveau niveau, et l'ancien
                // niveau sous forme de String
                User user = new User();
                user.setId(123L);
                SubscriptionLevel newLevel = new SubscriptionLevel();
                newLevel.setId(456L);
                newLevel.setLevel(Level.PREMIUM);
                Subscription subscription = new Subscription();
                subscription.setId(789L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(newLevel);
                String oldLevelString = "BASIC";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 789L,
                                "userId", 123L,
                                "newLevelId", 456L,
                                "newLevelName", "PREMIUM",
                                "oldLevel", oldLevelString,
                                "changeTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionLevelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription,
                                oldLevelString);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.level.changed",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la publication correcte d'un événement de changement de
        // niveau d'abonnement sans utilisateur
        @Test
        void publishSubscriptionLevelChangedEvent_shouldPublishCorrectlyWithoutUser() throws Exception {
                // GIVEN : Un abonnement sans utilisateur, un nouveau niveau, et l'ancien niveau
                SubscriptionLevel newLevel = new SubscriptionLevel();
                newLevel.setId(101L);
                newLevel.setLevel(Level.BASIC);
                Subscription subscription = new Subscription();
                subscription.setId(202L);
                subscription.setUser(null);
                subscription.setSubscriptionLevel(newLevel);
                String oldLevelString = "FREE";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 202L,
                                "userId", null,
                                "newLevelId", 101L,
                                "newLevelName", "STANDARD",
                                "oldLevel", oldLevelString,
                                "changeTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionLevelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription,
                                oldLevelString);

                // THEN : Vérification de l'appel à RabbitTemplate
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.level.changed",
                                messageCaptor.capture());
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
                assertEquals(expectedMessage, messageCaptor.getValue());
        }

        // Test pour vérifier la publication correcte avec un nouveau niveau
        // d'abonnement null
        @Test
        void publishSubscriptionLevelChangedEvent_shouldPublishCorrectlyWithNullNewLevel() throws Exception {
                // GIVEN : Un abonnement avec un utilisateur, un nouveau niveau null, et
                // l'ancien niveau
                User user = new User();
                user.setId(303L);
                Subscription subscription = new Subscription();
                subscription.setId(404L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(null);
                String oldLevelString = "PREMIUM";

                // AND : Configuration de l'ObjectMapper
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 404L,
                                "userId", 303L,
                                "newLevelId", null,
                                "newLevelName", null,
                                "oldLevel", oldLevelString,
                                "changeTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionLevelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription,
                                oldLevelString);

                // THEN : Vérification de l'appel à RabbitTemplate
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.level.changed",
                                messageCaptor.capture());
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
                assertEquals(expectedMessage, messageCaptor.getValue());
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation et log
        // de l'erreur
        @Test
        void publishSubscriptionLevelChangedEvent_shouldHandleSerializationException_andLogError() throws Exception {
                // GIVEN : Un abonnement et l'ancien niveau
                Subscription subscription = new Subscription();
                subscription.setId(505L);
                String oldLevelString = "STANDARD";

                // AND : Configuration de l'ObjectMapper pour lancer une exception
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionLevelChangedEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionLevelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription,
                                        oldLevelString);

                        // THEN : Vérification de l'appel à RabbitTemplate (le message pourrait être
                        // null)
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.level.changed",
                                        (String) null);
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'Évènement SubscriptionLevelChangedEvent : {}"),
                                        eq(errorMessage),
                                        any(Exception.class));
                }
        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi via
        // RabbitTemplate et log de l'erreur
        @Test
        void publishSubscriptionLevelChangedEvent_shouldHandleRabbitTemplateException_andLogError() throws Exception {
                // GIVEN : Un abonnement et l'ancien niveau
                Subscription subscription = new Subscription();
                subscription.setId(606L);
                String oldLevelString = "BASIC";
                Map<String, Object> payload = Map.of(
                                "subscriptionId", 606L,
                                "userId", null,
                                "newLevelId", null,
                                "newLevelName", null,
                                "oldLevel", oldLevelString,
                                "changeTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(payload);
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // AND : Mock du RabbitTemplate pour lancer une exception
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                "subscription.events",
                                                "subscription.level.changed",
                                                expectedMessage);

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionLevelChangedEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionLevelChangedEventPublisher.publishSubscriptionLevelChangedEvent(subscription,
                                        oldLevelString);

                        // THEN : Vérification de l'appel à RabbitTemplate
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.level.changed",
                                        expectedMessage);
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'Évènement SubscriptionLevelChangedEvent : {}"),
                                        eq(errorMessage),
                                        any(RuntimeException.class));
                }
        }
}