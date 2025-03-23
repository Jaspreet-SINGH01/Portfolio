package com.videoflix.users_microservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    @RabbitListener(queues = "myQueue")
    public void receiveMessage(String message) {
        logger.info("Received message: {}", message);
    }
}