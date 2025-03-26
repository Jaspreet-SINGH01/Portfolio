package com.videoflix.Users.async;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.videoflix.users_microservice.async.AsyncConfig;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void taskExecutor_ShouldReturnThreadPoolTaskExecutor_WithCorrectConfiguration() {
        // Création d'un contexte d'application Spring pour récupérer le bean
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class);

        // Récupération du bean Executor nommé "taskExecutor"
        Executor executor = context.getBean("taskExecutor", Executor.class);

        // Vérification que le bean récupéré est une instance de ThreadPoolTaskExecutor
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        // Cast de l'Executor en ThreadPoolTaskExecutor pour vérifier les configurations
        // spécifiques
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        // Vérification des configurations du ThreadPoolTaskExecutor
        assertEquals(5, threadPoolExecutor.getCorePoolSize()); // Vérification du nombre de threads dans le pool
        assertEquals(10, threadPoolExecutor.getMaxPoolSize()); // Vérification du nombre maximal de threads dans le pool
        assertEquals(25, threadPoolExecutor.getQueueCapacity()); // Vérification de la taille de la file d'attente des
                                                                 // tâches
        assertEquals(60, threadPoolExecutor.getKeepAliveSeconds()); // Vérification du temps d'inactivité maximal des
                                                                    // threads
        assertTrue(threadPoolExecutor.getThreadNamePrefix().startsWith("Async-")); // Vérification du préfixe des noms
                                                                                   // de threads

        // Fermeture du contexte d'application Spring
        context.close();
    }
}