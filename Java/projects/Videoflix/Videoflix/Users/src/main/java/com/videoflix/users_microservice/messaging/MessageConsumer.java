package com.videoflix.users_microservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    /**
     * Méthode pour écouter les messages provenant de la queue RabbitMQ "myQueue".
     * Cette méthode est annotée avec @RabbitListener, ce qui signifie qu'elle est
     * automatiquement
     * invoquée lorsqu'un message est reçu dans la queue spécifiée.
     *
     * @param message Le message reçu de la queue RabbitMQ.
     */
    @RabbitListener(queues = "myQueue")
    public void receiveMessage(String message) {
        // Journalise le message reçu.
        logger.info("Received message: {}", message);
    }
}