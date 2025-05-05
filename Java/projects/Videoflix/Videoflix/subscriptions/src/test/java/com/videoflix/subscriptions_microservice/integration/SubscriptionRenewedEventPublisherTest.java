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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewedEventPublisherTest {

        @Mock
        private RabbitTemplate rabbitTemplate;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private SubscriptionRenewedEventPublisher subscriptionRenewedEventPublisher;

        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement de renouvellement
        // d'abonnement avec toutes les informations
        @Test
        void publishSubscriptionRenewedEvent_shouldPublishCorrectlyWithAllInfo() throws Exception {
                // GIVEN : Un abonnement avec un utilisateur, un niveau d'abonnement et une date
                // de prochaine facturation
                User user = new User();
                user.setId(123L);
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setId(456L);
                subscriptionLevel.setLevel(Level.PREMIUM);
                Subscription subscription = new Subscription();
                subscription.setId(789L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(subscriptionLevel);
                subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 789L,
                                "userId", 123L,
                                "renewalDate", LocalDateTime.now().toString(),
                                "nextBillingDate", LocalDate.now().plusMonths(1).toString(),
                                "subscriptionLevelId", 456L,
                                "subscriptionLevelName", "PREMIUM");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.renewed",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la publication correcte d'un événement de renouvellement
        // d'abonnement sans utilisateur
        @Test
        void publishSubscriptionRenewedEvent_shouldPublishCorrectlyWithoutUser() throws Exception {
                // GIVEN : Un abonnement sans utilisateur, mais avec un niveau et une date de
                // prochaine facturation
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setId(101L);
                subscriptionLevel.setLevel(Level.BASIC);
                Subscription subscription = new Subscription();
                subscription.setId(202L);
                subscription.setUser(null);
                subscription.setSubscriptionLevel(subscriptionLevel);
                subscription.setNextBillingDate(LocalDateTime.now().plusWeeks(2));

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 202L,
                                "userId", null,
                                "renewalDate", LocalDateTime.now().toString(),
                                "nextBillingDate", LocalDate.now().plusWeeks(2).toString(),
                                "subscriptionLevelId", 101L,
                                "subscriptionLevelName", "BASIC");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);

                // THEN : Vérification de l'appel à RabbitTemplate
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.renewed",
                                messageCaptor.capture());
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
                assertEquals(expectedMessage, messageCaptor.getValue());
        }

        // Test pour vérifier la publication correcte avec une date de prochaine
        // facturation nulle
        @Test
        void publishSubscriptionRenewedEvent_shouldPublishCorrectlyWithNullNextBillingDate() throws Exception {
                // GIVEN : Un abonnement avec un utilisateur et un niveau, mais sans date de
                // prochaine facturation
                User user = new User();
                user.setId(303L);
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setId(505L);
                subscriptionLevel.setLevel(Level.BASIC);
                Subscription subscription = new Subscription();
                subscription.setId(404L);
                subscription.setUser(user);
                subscription.setSubscriptionLevel(subscriptionLevel);
                subscription.setNextBillingDate(null);

                // AND : Configuration de l'ObjectMapper
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 404L,
                                "userId", 303L,
                                "renewalDate", LocalDateTime.now().toString(),
                                "nextBillingDate", null,
                                "subscriptionLevelId", 505L,
                                "subscriptionLevelName", "STANDARD");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);

                // THEN : Vérification de l'appel à RabbitTemplate
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "subscription.events",
                                "subscription.renewed",
                                messageCaptor.capture());
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
                assertEquals(expectedMessage, messageCaptor.getValue());
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation et log
        // de l'erreur
        @Test
        void publishSubscriptionRenewedEvent_shouldHandleSerializationException_andLogError() throws Exception {
                // GIVEN : Un abonnement
                Subscription subscription = new Subscription();
                subscription.setId(505L);

                // AND : Configuration de l'ObjectMapper pour lancer une exception
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionRenewedEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);

                        // THEN : Vérification de l'appel à RabbitTemplate (le message pourrait être
                        // null)
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.renewed",
                                        (String) null);
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'évènement SubscriptionRenewedEvent : {}"),
                                        eq(errorMessage),
                                        any(Exception.class));
                }
        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi via
        // RabbitTemplate et log de l'erreur
        @Test
        void publishSubscriptionRenewedEvent_shouldHandleRabbitTemplateException_andLogError() throws Exception {
                // GIVEN : Un abonnement
                Subscription subscription = new Subscription();
                subscription.setId(606L);
                Map<String, Object> payload = Map.of(
                                "subscriptionId", 606L,
                                "userId", null,
                                "renewalDate", LocalDateTime.now().toString(),
                                "nextBillingDate", null,
                                "subscriptionLevelId", null,
                                "subscriptionLevelName", null);
                String expectedMessage = new ObjectMapper().writeValueAsString(payload);
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // AND : Mock du RabbitTemplate pour lancer une exception
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                "subscription.events",
                                                "subscription.renewed",
                                                expectedMessage);

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(SubscriptionRenewedEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        subscriptionRenewedEventPublisher.publishSubscriptionRenewedEvent(subscription);

                        // THEN : Vérification de l'appel à RabbitTemplate
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "subscription.events",
                                        "subscription.renewed",
                                        expectedMessage);
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'évènement SubscriptionRenewedEvent : {}"),
                                        eq(errorMessage),
                                        any(RuntimeException.class));
                }
        }
}