package com.videoflix.subscriptions_microservice.batch.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InactiveSubscriptionCleanupSchedulerTest {

    @InjectMocks
    private InactiveSubscriptionCleanupScheduler scheduler; // L'instance du scheduler à tester

    @Mock
    private JobLauncher jobLauncher; // Mock du JobLauncher

    @Mock
    private Job cleanupInactiveSubscriptionsJob; // Mock du Job

    @Test
    void runCleanupJob_shouldLaunchJobWithUniqueParameters() throws JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        // GIVEN : Les mocks sont configurés (par défaut, ils ne font rien de spécial)

        // WHEN : La méthode runCleanupJob est appelée
        scheduler.runCleanupJob();

        // THEN : Vérification que le JobLauncher a été appelé avec le bon Job et des
        // paramètres uniques
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        ArgumentCaptor<JobParameters> jobParametersCaptor = ArgumentCaptor.forClass(JobParameters.class);

        verify(jobLauncher, times(1)).run(jobCaptor.capture(), jobParametersCaptor.capture());

        // Vérification que le Job lancé est le bon
        assertEquals(cleanupInactiveSubscriptionsJob, jobCaptor.getValue());

        // Vérification que les paramètres du Job contiennent un paramètre "time" unique
        JobParameters capturedJobParameters = jobParametersCaptor.getValue();
        assertNotNull(capturedJobParameters.getLong("time"));

        // Pour s'assurer de l'unicité (dans un contexte de tests rapides),
        // on pourrait capturer plusieurs appels si le scheduler était appelé plusieurs
        // fois et vérifier que les valeurs de "time" sont différentes.
    }

    @Test
    void runCleanupJob_shouldHandleJobLauncherExceptions() throws Exception {
        // GIVEN : Configuration du mock du JobLauncher pour lancer une exception

        JobExecutionAlreadyRunningException alreadyRunningException = new JobExecutionAlreadyRunningException(
                "Job already running");
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(alreadyRunningException);

        // WHEN : Tentative d'exécution du job
        try {
            scheduler.runCleanupJob();
            // Si aucune exception n'est levée, le test échoue
            fail("Une exception JobExecutionAlreadyRunningException aurait dû être levée.");
        } catch (JobExecutionAlreadyRunningException e) {
            // THEN : Vérification que l'exception attendue a été levée
            assertEquals("Job already running", e.getMessage());
            verify(jobLauncher, times(1)).run(eq(cleanupInactiveSubscriptionsJob), any(JobParameters.class));
        }

        // Test pour JobRestartException
        JobRestartException restartException = new JobRestartException("Job can't be restarted");
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(restartException);

        try {
            scheduler.runCleanupJob();
            fail("Une exception JobRestartException aurait dû être levée.");
        } catch (JobRestartException e) {
            assertEquals("Job can't be restarted", e.getMessage());
            verify(jobLauncher, times(2)).run(eq(cleanupInactiveSubscriptionsJob), any(JobParameters.class));
        }

        // Test pour JobInstanceAlreadyCompleteException
        JobInstanceAlreadyCompleteException completeException = new JobInstanceAlreadyCompleteException(
                "Job instance already complete");
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(completeException);

        try {
            scheduler.runCleanupJob();
            fail("Une exception JobInstanceAlreadyCompleteException aurait dû être levée.");
        } catch (JobInstanceAlreadyCompleteException e) {
            assertEquals("Job instance already complete", e.getMessage());
            verify(jobLauncher, times(3)).run(eq(cleanupInactiveSubscriptionsJob), any(JobParameters.class));
        }

        // Test pour JobParametersInvalidException
        JobParametersInvalidException invalidParametersException = new JobParametersInvalidException(
                "Invalid job parameters");
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(invalidParametersException);

        try {
            scheduler.runCleanupJob();
            fail("Une exception JobParametersInvalidException aurait dû être levée.");
        } catch (JobParametersInvalidException e) {
            assertEquals("Invalid job parameters", e.getMessage());
            verify(jobLauncher, times(4)).run(eq(cleanupInactiveSubscriptionsJob), any(JobParameters.class));
        }
    }
}