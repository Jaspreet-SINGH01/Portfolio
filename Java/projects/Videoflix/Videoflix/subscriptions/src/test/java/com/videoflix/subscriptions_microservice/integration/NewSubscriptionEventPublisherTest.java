package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.Subscription.SubscriptionStatus;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.Level;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.BillingFrequency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.NEW_SUBSCRIPTION_EXCHANGE;
import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.WELCOME_EMAIL_ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewSubscriptionEventPublisherTest {

        // Mock du RabbitTemplate pour vérifier l'interaction avec RabbitMQ
        @Mock
        private RabbitTemplate rabbitTemplate;

        // Mock de l'ObjectMapper pour vérifier la sérialisation de l'événement
        @Mock
        private ObjectMapper objectMapper;

        // Instance de la classe à tester, avec les mocks injectés
        @InjectMocks
        private NewSubscriptionEventPublisher newSubscriptionEventPublisher;

        // Captor pour capturer l'argument 'message' passé à
        // rabbitTemplate.convertAndSend
        @Captor
        private ArgumentCaptor<String> messageCaptor;

        // Test pour vérifier la publication correcte d'un événement de nouvelle
        // souscription
        @Test
        void publishNewSubscriptionEvent_shouldPublishCorrectly() throws Exception {
                // GIVEN : Un événement de nouvelle souscription à publier
                User user = new User();
                user.setId(123L);

                Subscription subscription = new Subscription();
                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setLevel(Level.PREMIUM);
                subscriptionLevel.setPrice(9.99);
                subscriptionLevel.setFeatures("Feature 1, Feature 2, Feature 3");
                subscriptionLevel.setDescription("Premium subscription with all features");
                subscriptionLevel.setBillingFrequency(BillingFrequency.MONTHLY);
                subscriptionLevel.setStripePriceId("price_123");
                subscription.setSubscriptionLevel(subscriptionLevel);
                subscription.setStartDate(LocalDateTime.now());
                subscription.setEndDate(LocalDateTime.now().plusMonths(1));
                subscription.setCreationTimestamp(LocalDateTime.now());
                subscription.setStatus(SubscriptionStatus.ACTIVE);

                NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(this, user, subscription);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON lors de
                // la sérialisation de l'événement
                String expectedMessage = "{\"userId\":123,\"subscriptionLevelName\":\"PREMIUM\",\"startDate\":\""
                                + LocalDateTime.now() + "\",\"endDate\":\"" + LocalDateTime.now().plusMonths(1)
                                + "\",\"createdAt\":\"" + LocalDateTime.now() + "\"}";
                org.mockito.Mockito.when(objectMapper.writeValueAsString(event)).thenReturn(expectedMessage);

                // WHEN : Publication de l'événement
                newSubscriptionEventPublisher.publishNewSubscriptionEvent(event);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                NEW_SUBSCRIPTION_EXCHANGE, // Vérification de l'échange
                                WELCOME_EMAIL_ROUTING_KEY, // Vérification de la clé de routage
                                messageCaptor.capture() // Capture du message publié
                );

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser l'événement
                verify(objectMapper, times(1)).writeValueAsString(event);

                // Vérification que le message publié est celui attendu
                assertEquals(expectedMessage, messageCaptor.getValue(),
                                "Le message publié sur RabbitMQ doit correspondre à l'événement sérialisé.");
        }

        // Test pour vérifier la gestion d'une exception lors de la sérialisation de
        // l'événement
        @Test
        void publishNewSubscriptionEvent_shouldHandleSerializationException() throws Exception {
                // GIVEN : Un événement de nouvelle souscription à publier
                User user = new User();
                user.setId(456L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPassword("password");
                user.setFirstname("Test");
                user.setLastname("User");

                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setStartDate(LocalDateTime.now());
                subscription.setEndDate(LocalDateTime.now().plusWeeks(1));
                subscription.setStatus(SubscriptionStatus.ACTIVE);

                NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(
                                this, // source can be any object, using 'this' as example
                                user,
                                subscription);

                // AND : Configuration de l'ObjectMapper pour lancer une exception lors de la
                // sérialisation
                String errorMessage = "Erreur de sérialisation JSON";
                org.mockito.Mockito.when(objectMapper.writeValueAsString(event)).thenThrow(new Exception(errorMessage));

                // WHEN : Publication de l'événement
                newSubscriptionEventPublisher.publishNewSubscriptionEvent(event);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois (même si le message sera potentiellement null ou incorrect
                // selon l'implémentation interne)
                verify(rabbitTemplate, times(1)).convertAndSend(
                                NEW_SUBSCRIPTION_EXCHANGE,
                                WELCOME_EMAIL_ROUTING_KEY,
                                (String) null // Dans le cas d'une exception, le message pourrait être null
                );

                // Vérification que l'ObjectMapper a tenté de sérialiser l'événement
                verify(objectMapper, times(1)).writeValueAsString(event);

                // On pourrait également vérifier les logs pour s'assurer que l'erreur de
                // sérialisation a été enregistrée
                // (cela nécessiterait potentiellement de mocker le Logger)
        }

        // Test pour vérifier la gestion d'une exception lors de l'envoi du message via
        // RabbitTemplate
        @Test
        void publishNewSubscriptionEvent_shouldHandleRabbitTemplateException() throws Exception {
                // GIVEN : Un événement de nouvelle souscription à publier
                User user = new User();
                user.setId(789L);

                SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
                subscriptionLevel.setLevel(SubscriptionLevel.Level.PREMIUM);

                Subscription subscription = new Subscription();
                subscription.setSubscriptionLevel(subscriptionLevel);
                subscription.setStartDate(LocalDateTime.now());
                subscription.setEndDate(LocalDateTime.now().plusMonths(3));
                subscription.setCreationTimestamp(LocalDateTime.now());

                NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(this, user, subscription);

                // AND : Configuration de l'ObjectMapper pour retourner une chaîne JSON
                String expectedMessage = "{\"userId\":789,\"subscriptionLevelName\":\"STANDARD\",\"startDate\":\""
                                + LocalDateTime.now() + "\",\"endDate\":\"" + LocalDateTime.now().plusMonths(3)
                                + "\",\"createdAt\":\"" + LocalDateTime.now() + "\"}";
                org.mockito.Mockito.when(objectMapper.writeValueAsString(event)).thenReturn(expectedMessage);

                // AND : Configuration du RabbitTemplate pour lancer une exception lors de
                // l'envoi
                String errorMessage = "Erreur lors de l'envoi à RabbitMQ";
                org.mockito.Mockito.doThrow(new RuntimeException(errorMessage))
                                .when(rabbitTemplate).convertAndSend(
                                                NEW_SUBSCRIPTION_EXCHANGE,
                                                WELCOME_EMAIL_ROUTING_KEY,
                                                expectedMessage);

                // WHEN : Publication de l'événement
                newSubscriptionEventPublisher.publishNewSubscriptionEvent(event);

                // THEN : Vérification que la méthode convertAndSend du RabbitTemplate a été
                // appelée une fois
                verify(rabbitTemplate, times(1)).convertAndSend(
                                NEW_SUBSCRIPTION_EXCHANGE,
                                WELCOME_EMAIL_ROUTING_KEY,
                                expectedMessage);

                // Vérification que l'ObjectMapper a été utilisé pour sérialiser l'événement
                verify(objectMapper, times(1)).writeValueAsString(event);

                // On pourrait également vérifier les logs pour s'assurer que l'erreur de
                // publication a été enregistrée
                // (cela nécessiterait potentiellement de mocker le Logger)
        }
}