package com.videoflix.subscriptions_microservice.batch.reader;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class InactiveSubscriptionReader implements ItemReader<Subscription> {

    private final RepositoryItemReader<Subscription> reader;

    public InactiveSubscriptionReader(SubscriptionRepository subscriptionRepository, LocalDateTime cutoffDate) {
        reader = new RepositoryItemReader<>();
        reader.setRepository(subscriptionRepository);
        reader.setMethodName("findByStatusAndLastActivityBefore");
        reader.setArguments(List.of(Subscription.SubscriptionStatus.INACTIVE, cutoffDate));
        reader.setSort(Collections.singletonMap("id", Sort.Direction.ASC)); // Ordre de lecture
        reader.setPageSize(100); // Taille de la page pour la lecture
    }

    @Override
    public Subscription read() throws Exception {
        return reader.read();
    }
}