package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodUpdatedEventPublisherTest {

        // Mock du RabbitTemplate pour vérifier l'interaction avec RabbitMQ
        @Mock
        private RabbitTemplate rabbitTemplate;

        // Mock de l'ObjectMapper pour vérifier la sérialisation de l'événement
        @Mock
        private ObjectMapper objectMapper;

        // Instance de la classe à tester, avec les mocks injectés
        @InjectMocks
        private PaymentMethodUpdatedEventPublisher paymentMethodUpdatedEventPublisher;

        // Captor pour capturer l'argument 'message' passé à
        // rabbitTemplate.convertAndSend
        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement de mise à jour de
        // la méthode de paiement
        @Test
        void publishPaymentMethodUpdatedEvent_shouldPublishCorrectly() throws Exception {
                // GIVEN : Un utilisateur dont la méthode de paiement a été mise à jour
                User user = new User();
                user.setId(123L);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "userId", 123L,
                                "updateTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload); // Utiliser un nouvel
                                                                                                 // ObjectMapper pour la
                                                                                                 // comparaison

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                paymentMethodUpdatedEventPublisher.publishPaymentMethodUpdatedEvent(user);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "user.events", // Vérification de l'échange
                                "user.payment.method.updated", // Vérification de la clé de routage
                                messageCaptor.capture() // Capture du message publié
                );

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation de
        // l'événement
        @Test
        void publishPaymentMethodUpdatedEvent_shouldHandleSerializationException() throws Exception {
                // GIVEN : Un utilisateur
                User user = new User();
                user.setId(456L);

                // AND : Configuration de l'ObjectMapper pour lancer une exception lors de la
                // sérialisation
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // WHEN : Publication de l'événement
                paymentMethodUpdatedEventPublisher.publishPaymentMethodUpdatedEvent(user);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois (même si le message sera potentiellement null ou incorrect)
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "user.events",
                                "user.payment.method.updated",
                                (String) null // Le message pourrait être null en cas d'exception
                );

                // Vérification que l'ObjectMapper a tenté de sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi du message via
        // RabbitTemplate
        @Test
        void publishPaymentMethodUpdatedEvent_shouldHandleRabbitTemplateException() throws Exception {
                // GIVEN : Un utilisateur
                User user = new User();
                user.setId(789L);
                Map<String, Object> payload = Map.of(
                                "userId", 789L,
                                "updateTimestamp", LocalDateTime.now().toString());
                String expectedMessage = new ObjectMapper().writeValueAsString(payload);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // AND : Configuration du RabbitTemplate pour lancer une exception lors de
                // l'envoi
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                org.mockito.Mockito.doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                "user.events",
                                                "user.payment.method.updated",
                                                expectedMessage);

                // WHEN : Publication de l'événement
                paymentMethodUpdatedEventPublisher.publishPaymentMethodUpdatedEvent(user);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "user.events",
                                "user.payment.method.updated",
                                expectedMessage);

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
        }

        @Test
        void publishPaymentMethodUpdatedEvent_shouldHandleSerializationException_andLogError() throws Exception {
                // GIVEN : Un utilisateur
                User user = new User();
                user.setId(456L);

                // AND : Configuration de l'ObjectMapper pour lancer une exception lors de la
                // sérialisation
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // AND : Mock du Logger
                Logger mockLogger = mock(Logger.class);

                // AND : Mock du LoggerFactory pour qu'il retourne notre mock de Logger
                try (MockedStatic<LoggerFactory> factory = mockStatic(LoggerFactory.class)) {
                        factory.when(() -> LoggerFactory.getLogger(PaymentMethodUpdatedEventPublisher.class))
                                        .thenReturn(mockLogger);

                        // WHEN : Publication de l'événement
                        paymentMethodUpdatedEventPublisher.publishPaymentMethodUpdatedEvent(user);

                        // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                        // appelée (même si le message sera potentiellement null)
                        verify(rabbitTemplate, times(1)).convertAndSend(
                                        "user.events",
                                        "user.payment.method.updated",
                                        (String) null);

                        // THEN : Vérification que l'ObjectMapper a tenté de sérialiser le payload
                        verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                        // THEN : Vérification que le Logger a enregistré une erreur avec le message
                        // d'erreur de l'exception
                        verify(mockLogger, times(1)).error(
                                        eq("Erreur lors de la publication de l'Évènement PaymentMethodUpdatedEvent : {}"),
                                        eq(errorMessage),
                                        any(Exception.class) // Vérifie qu'une instance de l'exception est passée
                        );
                }
        }
}