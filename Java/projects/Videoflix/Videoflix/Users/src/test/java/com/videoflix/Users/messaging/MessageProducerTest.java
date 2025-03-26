package com.videoflix.Users.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.videoflix.users_microservice.messaging.MessageProducer;

import static org.mockito.Mockito.*;

class MessageProducerTest {

    // Mock pour RabbitTemplate
    @Mock
    private RabbitTemplate rabbitTemplate;

    // Instance du producteur de messages à tester
    private MessageProducer messageProducer;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        MockitoAnnotations.openMocks(this);

        // Création de l'instance de MessageProducer avec le mock RabbitTemplate
        messageProducer = new MessageProducer(rabbitTemplate);
    }

    @Test
    void testSendMessage_NormalMessage() {
        // Préparation du message de test
        String testMessage = "Message de test pour RabbitMQ";

        // Appel de la méthode à tester
        messageProducer.sendMessage(testMessage);

        // Vérification que le message a été envoyé sur la bonne file d'attente
        verify(rabbitTemplate).convertAndSend("myQueue", testMessage);
    }

    @Test
    void testSendMessage_NullMessage() {
        // Appel de la méthode à tester avec un message null
        messageProducer.sendMessage(null);
        // Aucun message ne devrait être envoyé sur la file d'attente car le message est null
        verify(rabbitTemplate, never()).convertAndSend("myQueue", (Object)null);
    }

    @Test
    void testSendMessage_EmptyMessage() {
        // Préparation d'un message vide
        String emptyMessage = "";

        // Appel de la méthode à tester
        messageProducer.sendMessage(emptyMessage);

        // Vérification que le message vide est envoyé sur la file d'attente
        verify(rabbitTemplate).convertAndSend("myQueue", emptyMessage);
    }

    @Test
    void testSendMessage_LongMessage() {
        // Préparation d'un message long
        String longMessage = "A".repeat(1000);

        // Appel de la méthode à tester
        messageProducer.sendMessage(longMessage);

        // Vérification que le message long est envoyé sur la file d'attente
        verify(rabbitTemplate).convertAndSend("myQueue", longMessage);
    }

    @Test
    void testSendMessage_MultipleMessages() {
        // Préparation de plusieurs messages
        String[] messages = {
            "Premier message",
            "Deuxième message",
            "Troisième message"
        };

        // Envoi de chaque message
        for (String message : messages) {
            messageProducer.sendMessage(message);
        }

        // Vérification que chaque message a été envoyé sur la file d'attente
        for (String message : messages) {
            verify(rabbitTemplate).convertAndSend("myQueue", message);
        }
    }
}