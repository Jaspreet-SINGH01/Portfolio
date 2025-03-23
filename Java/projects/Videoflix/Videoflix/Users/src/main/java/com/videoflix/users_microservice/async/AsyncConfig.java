package com.videoflix.users_microservice.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Nombre de threads dans le pool
        executor.setMaxPoolSize(10); // Nombre maximal de threads dans le pool
        executor.setQueueCapacity(25); // Taille de la file d'attente des tâches
        executor.setKeepAliveSeconds(60); // Temps d'inactivité maximal des threads (en secondes)
        executor.setThreadNamePrefix("Async-"); // Préfixe des noms de threads
        executor.initialize();
        return executor;
    }
}