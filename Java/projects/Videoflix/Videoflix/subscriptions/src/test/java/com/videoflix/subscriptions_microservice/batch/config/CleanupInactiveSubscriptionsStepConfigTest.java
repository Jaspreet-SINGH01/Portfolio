package com.videoflix.subscriptions_microservice.batch.config;

import com.videoflix.subscriptions_microservice.batch.processor.InactiveSubscriptionProcessor;
import com.videoflix.subscriptions_microservice.batch.reader.InactiveSubscriptionReader;
import com.videoflix.subscriptions_microservice.batch.writer.InactiveSubscriptionWriter;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = CleanupInactiveSubscriptionsStepConfigTest.BatchConfig.class)
class CleanupInactiveSubscriptionsStepConfigTest {

    @Autowired
    private Step cleanupInactiveSubscriptionsStep;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InactiveSubscriptionProcessor inactiveSubscriptionProcessor;

    @Autowired
    private InactiveSubscriptionWriter inactiveSubscriptionWriter;

    @Autowired
    private InactiveSubscriptionReader inactiveSubscriptionReader;

    @Autowired
    private CleanupInactiveSubscriptionsStepConfig config;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        config = new CleanupInactiveSubscriptionsStepConfig(
                jobRepository,
                transactionManager,
                subscriptionRepository,
                inactiveSubscriptionProcessor,
                inactiveSubscriptionWriter);
    }

    @Test
    void cleanupInactiveSubscriptionsStep_shouldReadProcessAndWriteInactiveSubscriptions() throws Exception {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(90, ChronoUnit.DAYS);
        List<Subscription> inactiveSubscriptions = Arrays.asList(
                new Subscription());

        Subscription activeSubscription = new Subscription();

        subscriptionRepository.save(activeSubscription);

        when(subscriptionRepository.findByEndDateBeforeAndStatusIn(
                eq(cutoffDate), anyList())).thenReturn(inactiveSubscriptions);

        when(inactiveSubscriptionProcessor.process(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Step step = config.cleanupInactiveSubscriptionsStep();

        JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobRepository(jobRepository);

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                step.getName(),
                new JobParameters());

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndStatusIn(eq(cutoffDate), anyList());
        verify(inactiveSubscriptionProcessor, times(inactiveSubscriptions.size())).process(any(Subscription.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Subscription>> subscriptionCaptor = ArgumentCaptor.forClass(List.class);
        verify(inactiveSubscriptionWriter).write(subscriptionCaptor.capture());
        assertEquals(inactiveSubscriptions.size(), subscriptionCaptor.getValue().size());
        assertTrue(subscriptionCaptor.getValue().containsAll(inactiveSubscriptions));
    }

    @Configuration
    static class BatchConfig {

        @Bean
        public JobRepository jobRepository(PlatformTransactionManager transactionManager, DataSource dataSource)
                throws Exception {
            JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
            factoryBean.setTransactionManager(transactionManager); // Transaction manager nécessaire
            factoryBean.setDataSource(dataSource); // Source de données
            factoryBean.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ"); // Optionnel, pour gérer les niveaux
                                                                                 // d'isolation
            factoryBean.setTablePrefix("BATCH_"); // Préciser le préfixe des tables si nécessaire
            factoryBean.afterPropertiesSet(); // Important pour initialiser correctement la configuration
            return factoryBean.getObject();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }

        @Bean
        public SubscriptionRepository subscriptionRepository() {
            return mock(SubscriptionRepository.class);
        }

        @Bean
        public InactiveSubscriptionProcessor inactiveSubscriptionProcessor() {
            return mock(InactiveSubscriptionProcessor.class);
        }

        @Bean
        public InactiveSubscriptionWriter inactiveSubscriptionWriter() {
            return mock(InactiveSubscriptionWriter.class);
        }

        @Bean
        public InactiveSubscriptionReader inactiveSubscriptionReader(SubscriptionRepository subscriptionRepository) {
            LocalDateTime cutoffDate = LocalDateTime.now().minus(90, ChronoUnit.DAYS);
            return new InactiveSubscriptionReader(subscriptionRepository, cutoffDate);
        }

        @Bean
        public Step cleanupInactiveSubscriptionsStep(
                JobRepository jobRepository,
                PlatformTransactionManager transactionManager,
                InactiveSubscriptionReader inactiveSubscriptionReader,
                InactiveSubscriptionProcessor inactiveSubscriptionProcessor,
                InactiveSubscriptionWriter inactiveSubscriptionWriter) {
            return new StepBuilder("cleanupInactiveSubscriptionsStep", jobRepository)
                    .<Subscription, Subscription>chunk(100, transactionManager)
                    .reader(inactiveSubscriptionReader)
                    .processor(inactiveSubscriptionProcessor)
                    .writer(inactiveSubscriptionWriter)
                    .build();
        }
    }
}
