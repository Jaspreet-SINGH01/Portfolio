package com.videoflix.subscriptions_microservice.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InactiveSubscriptionCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job cleanupInactiveSubscriptionsJob;

    public InactiveSubscriptionCleanupScheduler(JobLauncher jobLauncher, Job cleanupInactiveSubscriptionsJob) {
        this.jobLauncher = jobLauncher;
        this.cleanupInactiveSubscriptionsJob = cleanupInactiveSubscriptionsJob;
    }

    // Planification de l'exécution de cette tâche tous les jours à 02h00 du matin
    @Scheduled(cron = "0 0 2 * * *")
    @Async
    public void runCleanupJob() throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // Paramètre unique pour chaque exécution
                .toJobParameters();
        jobLauncher.run(cleanupInactiveSubscriptionsJob, jobParameters);
    }
}