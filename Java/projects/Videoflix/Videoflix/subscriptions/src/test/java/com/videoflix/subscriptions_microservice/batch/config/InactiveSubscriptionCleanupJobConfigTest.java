package com.videoflix.subscriptions_microservice.batch.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = InactiveSubscriptionCleanupJobConfigTest.BatchTestConfig.class)
class InactiveSubscriptionCleanupJobConfigTest {

    @Autowired
    private Job cleanupInactiveSubscriptionsJob;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Mock
    private Step cleanupInactiveSubscriptionsStep;

    @Test
    void cleanupInactiveSubscriptionsJob_shouldExecuteCleanupInactiveSubscriptionsStep() throws Exception {
        // GIVEN : Configuration du comportement mocké du Step

        // On simule que l'exécution du Step se termine sans erreur
        when(cleanupInactiveSubscriptionsStep.getName()).thenReturn("cleanupInactiveSubscriptionsStep");
        // Ici, on ne teste pas la logique interne du Step, seulement son exécution dans
        // le Job.
        // Si vous vouliez tester le résultat du Step, vous auriez besoin de mocks plus
        // complexes
        // et potentiellement d'utiliser StepRunner.

        // WHEN : Lancement du Job
        jobLauncherTestUtils.launchJob();

        // THEN : Vérification que le Step a été exécuté

        // Vérification que la méthode 'execute' du Step mocké a été appelée au moins
        // une fois
        // (l'exécution réelle du Step est gérée par Spring Batch, ici on vérifie
        // l'interaction)
        verify(cleanupInactiveSubscriptionsStep, times(1)).execute(any());
    }

    // Configuration Spring Batch de test
    @Configuration
    static class BatchTestConfig {

        @Bean
        public JobBuilder jobBuilder() {
            // Mock du JobBuilder pour construire le Job de test
            return mock(JobBuilder.class);
        }

        @Bean
        public Step cleanupInactiveSubscriptionsStep() {
            // Mock du Step pour vérifier son exécution
            return mock(Step.class);
        }

        @Bean
        public InactiveSubscriptionCleanupJobConfig inactiveSubscriptionCleanupJobConfig(JobBuilder jobBuilder,
                Step cleanupInactiveSubscriptionsStep) {
            // Instanciation de la configuration du Job avec les mocks
            return new InactiveSubscriptionCleanupJobConfig(jobBuilder, cleanupInactiveSubscriptionsStep);
        }

        @Bean
        public Job cleanupInactiveSubscriptionsJob(SimpleJobBuilder jobBuilder, Step cleanupInactiveSubscriptionsStep) {
            // Création du mock du Job
            Job jobMock = mock(Job.class);
            when(jobBuilder.start(cleanupInactiveSubscriptionsStep)).thenReturn(jobBuilder);
            when(jobBuilder.incrementer(any())).thenReturn(jobBuilder);
            when(jobBuilder.build()).thenReturn(jobMock);
            return jobMock;
        }

        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }
}