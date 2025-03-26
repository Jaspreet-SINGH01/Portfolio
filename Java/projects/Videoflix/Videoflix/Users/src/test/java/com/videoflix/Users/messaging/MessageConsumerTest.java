package com.videoflix.Users.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.videoflix.users_microservice.messaging.MessageConsumer;

import static org.mockito.Mockito.*;

class MessageConsumerTest {

    // Mock pour le logger
    @Mock
    private Logger mockLogger;

    // Instance du consommateur de messages à tester
    private MessageConsumer messageConsumer;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        MockitoAnnotations.openMocks(this);

        // Création d'une instance de MessageConsumer
        messageConsumer = new MessageConsumer() {
            // Remplacement du logger par le mock pour pouvoir vérifier les logs
            @Override
            protected Object clone() throws CloneNotSupportedException {
                MessageConsumer clone = (MessageConsumer) super.clone();
                // Utilisation de la réflexion pour remplacer le logger
                try {
                    java.lang.reflect.Field loggerField = MessageConsumer.class.getDeclaredField("logger");
                    loggerField.setAccessible(true);
                    loggerField.set(clone, mockLogger);
                } catch (Exception e) {
                    throw new RuntimeException("Impossible de remplacer le logger", e);
                }
                return clone;
            }
        };
    }

    @Test
    void testReceiveMessage_NormalMessage() {
        // Préparation du message de test
        String testMessage = "Message de test pour RabbitMQ";

        // Appel de la méthode à tester
        messageConsumer.receiveMessage(testMessage);

        // Vérification que le message a été loggué correctement
        verify(mockLogger).info("Received message: {}", testMessage);
    }

    @Test
    void testReceiveMessage_NullMessage() {
        // Appel de la méthode à tester avec un message null
        messageConsumer.receiveMessage(null);

        // Vérification que le message null est également loggué
        verify(mockLogger).info("Received message: null");
    }

    @Test
    void testReceiveMessage_EmptyMessage() {
        // Préparation d'un message vide
        String emptyMessage = "";

        // Appel de la méthode à tester
        messageConsumer.receiveMessage(emptyMessage);

        // Vérification que le message vide est loggué
        verify(mockLogger).info("Received message: {}", emptyMessage);
    }

    @Test
    void testReceiveMessage_LongMessage() {
        // Préparation d'un message long
        String longMessage = "A".repeat(1000);

        // Appel de la méthode à tester
        messageConsumer.receiveMessage(longMessage);

        // Vérification que le message long est loggué
        verify(mockLogger).info("Received message: {}", longMessage);
    }
}