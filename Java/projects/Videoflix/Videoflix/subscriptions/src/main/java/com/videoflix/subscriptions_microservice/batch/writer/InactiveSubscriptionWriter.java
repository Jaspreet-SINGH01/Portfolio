package com.videoflix.subscriptions_microservice.batch.writer;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InactiveSubscriptionWriter implements ItemWriter<Subscription> {

    private static final Logger logger = LoggerFactory.getLogger(InactiveSubscriptionWriter.class);
    private final SubscriptionRepository subscriptionRepository;

    public InactiveSubscriptionWriter(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void write(List<Subscription> chunk) throws Exception {
        List<? extends Subscription> subscriptionsToDelete = chunk.getItems();
        logger.info("Suppression de {} abonnements inactifs.", subscriptionsToDelete.size());
        subscriptionRepository.deleteAll(subscriptionsToDelete);
    }
}