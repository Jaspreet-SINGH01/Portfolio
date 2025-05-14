package com.videoflix.subscriptions_microservice.batch.config;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.batch.processor.InactiveSubscriptionProcessor;
import com.videoflix.subscriptions_microservice.batch.writer.InactiveSubscriptionWriter;
import com.videoflix.subscriptions_microservice.batch.reader.InactiveSubscriptionReader;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Configuration
public class CleanupInactiveSubscriptionsStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SubscriptionRepository subscriptionRepository;
    private final InactiveSubscriptionProcessor inactiveSubscriptionProcessor;
    private final InactiveSubscriptionWriter inactiveSubscriptionWriter;
    private static final int CHUNK_SIZE = 100;

    public CleanupInactiveSubscriptionsStepConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SubscriptionRepository subscriptionRepository,
            InactiveSubscriptionProcessor inactiveSubscriptionProcessor,
            InactiveSubscriptionWriter inactiveSubscriptionWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.subscriptionRepository = subscriptionRepository;
        this.inactiveSubscriptionProcessor = inactiveSubscriptionProcessor;
        this.inactiveSubscriptionWriter = inactiveSubscriptionWriter;
    }

    @Bean
    public Step cleanupInactiveSubscriptionsStep() {
        return new StepBuilder("cleanupInactiveSubscriptionsStep", jobRepository)
                .<Subscription, Subscription>chunk(CHUNK_SIZE, transactionManager)
                .reader(inactiveSubscriptionReader())
                .processor(inactiveSubscriptionProcessor)
                .writer(inactiveSubscriptionWriter)
                .build();
    }

    @Bean
    public InactiveSubscriptionReader inactiveSubscriptionReader() {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(90, ChronoUnit.DAYS);
        return new InactiveSubscriptionReader(subscriptionRepository, cutoffDate);
    }
}