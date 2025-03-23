package com.videoflix.users_microservice.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.videoflix.users_microservice.services.ConfigService;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.awt.image.BufferedImage;

public class AsyncTasks {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTasks.class);
    private final ConfigService configService;

    public AsyncTasks(ConfigService configService) {
        this.configService = configService;
    }

    // Exemple de gestion des ressources avec try-with-resources
    public void processFileAsync(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("Erreur lors du traitement du fichier", e);
        }
    }

    // Exemple de gestion des ressources avec finally
    public void processDatabaseAsync() {
        Connection connection = null;
        try {
            // Récupérer les informations de connexion depuis un fichier de configuration ou
            // des variables d'environnement
            String url = configService.getDatabaseUrl();
            String user = configService.getDatabaseUser();
            String password = configService.getDatabasePassword();

            connection = DriverManager.getConnection(url, user, password);
            logger.info("Connexion à la base de données établie.");
        } catch (SQLException e) {
            logger.error("Erreur de connexion à la base de données", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    logger.info("Connexion à la base de données fermée.");
                } catch (SQLException e) {
                    logger.error("Erreur lors de la fermeture de la connexion", e);
                }
            }
        }
    }

    // Exemple de gestion des objets volumineux
    public void processImageAsync(BufferedImage image) {
        try {
            logger.info("Traitement de l'image...");
        } finally {
            image.flush(); // Libération de la mémoire
            image = null;
            logger.info("Mémoire de l'image libérée.");
        }
    }

    // Exemple de temps limite avec CompletableFuture
    public CompletableFuture<String> processTaskWithTimeout() {
        return CompletableFuture.supplyAsync(() -> {
            // Logique de la tâche asynchrone
            try {
                TimeUnit.SECONDS.sleep(10); // Simulation d'une tâche longue
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Task completed";
        }).orTimeout(5, TimeUnit.SECONDS); // Temps limite de 5 secondes
    }

    public static void main(String[] args) {
        AsyncTasks asyncTasks = new AsyncTasks(null);

        // Exemple d'utilisation de processFileAsync
        asyncTasks.processFileAsync("test.txt");

        // Exemple d'utilisation de processDatabaseAsync
        asyncTasks.processDatabaseAsync();

        // Exemple d'utilisation de processImageAsync
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        asyncTasks.processImageAsync(image);

        // Exemple d'utilisation de processTaskWithTimeout
        try {
            String result = asyncTasks.processTaskWithTimeout().join();
            logger.info("Result: {}", result);
        } catch (Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.error("Task timed out");
            } else {
                logger.error("Erreur lors de l'exécution de la tâche", e);
            }
        }
    }
}