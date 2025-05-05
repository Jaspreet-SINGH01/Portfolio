package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentFailedEventPublisherTest {

        // Mock du RabbitTemplate pour vérifier l'interaction avec RabbitMQ
        @Mock
        private RabbitTemplate rabbitTemplate;

        // Mock de l'ObjectMapper pour vérifier la sérialisation de l'événement
        @Mock
        private ObjectMapper objectMapper;

        // Instance de la classe à tester, avec les mocks injectés
        @InjectMocks
        private PaymentFailedEventPublisher paymentFailedEventPublisher;

        // Captor pour capturer l'argument 'message' passé à
        // rabbitTemplate.convertAndSend
        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement de paiement échoué
        // avec un utilisateur associé
        @Test
        void publishPaymentFailedEvent_shouldPublishCorrectlyWithUser() throws Exception {
                // GIVEN : Un abonnement et une raison d'échec de paiement
                User user = new User();
                user.setId(789L);
                Subscription subscription = new Subscription();
                subscription.setId(123L);
                subscription.setUser(user);
                subscription.setNextRenewalDate(LocalDateTime.now().plusDays(7));
                subscription.setPriceId("price_abc");
                String failureReason = "Insufficient funds";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 123L,
                                "userId", 789L,
                                "failureTimestamp", LocalDateTime.now().toString(),
                                "failureReason", failureReason,
                                "nextRetryDate", LocalDate.now().plusDays(7).toString(),
                                "amountDue", "price_abc",
                                "currency", "EUR");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload); // Utiliser un nouvel
                                                                                                 // ObjectMapper pour la
                                                                                                 // comparaison

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                paymentFailedEventPublisher.publishPaymentFailedEvent(subscription, failureReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "billing.events", // Vérification de l'échange
                                "billing.payment.failed", // Vérification de la clé de routage
                                messageCaptor.capture() // Capture du message publié
                );

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la publication correcte d'un événement de paiement échoué
        // sans utilisateur associé
        @Test
        void publishPaymentFailedEvent_shouldPublishCorrectlyWithoutUser() throws Exception {
                // GIVEN : Un abonnement sans utilisateur et une raison d'échec de paiement
                Subscription subscription = new Subscription();
                subscription.setId(456L);
                subscription.setUser(null);
                subscription.setNextRenewalDate(LocalDateTime.now().plusDays(3));
                subscription.setPriceId("price_def");
                String failureReason = "Payment gateway error";

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation du payload
                Map<String, Object> expectedPayload = Map.of(
                                "subscriptionId", 456L,
                                "userId", null,
                                "failureTimestamp", LocalDateTime.now().toString(),
                                "failureReason", failureReason,
                                "nextRetryDate", LocalDate.now().plusDays(3).toString(),
                                "amountDue", "price_def",
                                "currency", "EUR");
                String expectedMessage = new ObjectMapper().writeValueAsString(expectedPayload);

                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                paymentFailedEventPublisher.publishPaymentFailedEvent(subscription, failureReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "billing.events",
                                "billing.payment.failed",
                                messageCaptor.capture());

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre au payload sérialisé.");
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation de
        // l'événement
        @Test
        void publishPaymentFailedEvent_shouldHandleSerializationException() throws Exception {
                // GIVEN : Un abonnement et une raison d'échec de paiement
                Subscription subscription = new Subscription();
                subscription.setId(789L);
                subscription.setUser(new User());
                String failureReason = "Serialization error";

                // AND : Configuration de l'ObjectMapper pour lancer une exception lors de la
                // sérialisation
                String errorMessage = "Erreur de sérialisation JSON";
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenThrow(new Exception(errorMessage));

                // WHEN : Publication de l'événement
                paymentFailedEventPublisher.publishPaymentFailedEvent(subscription, failureReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois (même si le message sera potentiellement null ou incorrect)
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "billing.events",
                                "billing.payment.failed",
                                (String) null // Le message pourrait être null en cas d'exception
                );

                // Vérification que l'ObjectMapper a tenté de sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi du message via
        // RabbitTemplate
        @Test
        void publishPaymentFailedEvent_shouldHandleRabbitTemplateException() throws Exception {
                // GIVEN : Un abonnement et une raison d'échec de paiement
                Subscription subscription = new Subscription();
                subscription.setId(999L);
                subscription.setUser(new User());
                String failureReason = "RabbitMQ connection error";
                Map<String, Object> payload = Map.of(
                                "subscriptionId", 999L,
                                "userId", 1L,
                                "failureTimestamp", LocalDateTime.now().toString(),
                                "failureReason", failureReason,
                                "nextRetryDate", null,
                                "amountDue", "price_xyz",
                                "currency", "EUR");
                String expectedMessage = new ObjectMapper().writeValueAsString(payload);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn(expectedMessage);

                // AND : Configuration du RabbitTemplate pour lancer une exception lors de
                // l'envoi
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                org.mockito.Mockito.doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                "billing.events",
                                                "billing.payment.failed",
                                                expectedMessage);

                // WHEN : Publication de l'événement
                paymentFailedEventPublisher.publishPaymentFailedEvent(subscription, failureReason);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                "billing.events",
                                "billing.payment.failed",
                                expectedMessage);

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser le payload
                verify(objectMapper, times(1)).writeValueAsString(org.mockito.ArgumentMatchers.anyMap());
        }
}