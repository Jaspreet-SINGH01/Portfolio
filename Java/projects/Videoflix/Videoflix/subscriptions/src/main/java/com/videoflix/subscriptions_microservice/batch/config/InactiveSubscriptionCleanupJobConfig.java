package com.videoflix.subscriptions_microservice.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class InactiveSubscriptionCleanupJobConfig {

    private final JobBuilder jobBuilder;
    private final Step cleanupInactiveSubscriptionsStep;

    public InactiveSubscriptionCleanupJobConfig(JobBuilder jobBuilder, Step cleanupInactiveSubscriptionsStep) {
        this.jobBuilder = jobBuilder;
        this.cleanupInactiveSubscriptionsStep = cleanupInactiveSubscriptionsStep;
    }

    @Bean
    public Job cleanupInactiveSubscriptionsJob() {
        return jobBuilder.start(cleanupInactiveSubscriptionsStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }
}