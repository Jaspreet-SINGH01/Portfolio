package com.videoflix.users_microservice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Constructeur pour MessageProducer.
     * Injecte le RabbitTemplate pour envoyer des messages à RabbitMQ.
     *
     * @param rabbitTemplate Le RabbitTemplate utilisé pour envoyer des messages.
     */
    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Méthode pour envoyer un message à la queue RabbitMQ "myQueue".
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        // Envoie le message à la queue "myQueue" en utilisant RabbitTemplate.
        rabbitTemplate.convertAndSend("myQueue", message);
    }
}