package com.videoflix.Users.async;

import com.videoflix.users_microservice.async.AsyncTasks;
import com.videoflix.users_microservice.services.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class AsyncTasksTest {

    @Mock
    private ConfigService configService; // Mock pour ConfigService

    @Mock
    private Logger logger; // Mock pour le Logger

    @InjectMocks
    private AsyncTasks asyncTasks; // Injection des mocks dans AsyncTasks

    private String filePath;
    private BufferedImage image;

    @BeforeEach
    void setUp() {
        filePath = "test.txt";
        image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }

    @Test
    void processFileAsync_ShouldLogLines_WhenFileExists() throws IOException {
        // Configuration du mock pour simuler la lecture d'un fichier
        BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine()).thenReturn("Line 1", "Line 2", null); // Simule la lecture de deux lignes

        // Utilisation de try-with-resources pour mocker FileReader
        try (MockedStatic<FileReader> fileReaderMockedStatic = mockStatic(FileReader.class)) {
            fileReaderMockedStatic.when(() -> new FileReader(filePath)).thenReturn(reader);

            // Appel de la méthode à tester
            asyncTasks.processFileAsync(filePath);

            // Vérification que le logger a été appelé avec les lignes du fichier
            verify(logger).info("Line 1");
            verify(logger).info("Line 2");
        }
    }

    @Test
    void processFileAsync_ShouldLogError_WhenIOExceptionOccurs() {
        // Configuration du mock pour lancer une IOException
        try (MockedStatic<FileReader> fileReaderMockedStatic = mockStatic(FileReader.class)) {
            fileReaderMockedStatic.when(() -> new FileReader(filePath)).thenThrow(new IOException("File not found"));

            // Appel de la méthode à tester
            asyncTasks.processFileAsync(filePath);

            // Vérification que le logger a été appelé avec l'erreur
            verify(logger).error(eq("Erreur lors du traitement du fichier"), any(IOException.class));
        }
    }

    @Test
    void processDatabaseAsync_ShouldLogConnectionAndDisconnection_WhenSuccessful() {
        // Configuration des mocks pour simuler une connexion réussie
        when(configService.getDatabaseUrl()).thenReturn("jdbc:h2:mem:testdb");
        when(configService.getDatabaseUser()).thenReturn("sa");
        when(configService.getDatabasePassword()).thenReturn("");

        // Utilisation de try-with-resources pour mocker DriverManager.getConnection
        try (MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class)) {
            Connection connection = mock(Connection.class);
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);

            // Appel de la méthode à tester
            asyncTasks.processDatabaseAsync();

            // Vérification que le logger a été appelé avec les messages de connexion et de
            // déconnexion
            verify(logger).info("Connexion à la base de données établie.");
            verify(logger).info("Connexion à la base de données fermée.");
            try {
                verify(connection).close();
            } catch (SQLException e) {
                // Ignorer l'exception car c'est un mock
            }
        }
    }

    @Test
    void processImageAsync_ShouldLogAndFlushImage() {
        // Appel de la méthode à tester
        asyncTasks.processImageAsync(image);

        // Vérification que le logger a été appelé et que l'image a été flushée
        verify(logger).info("Traitement de l'image...");
        verify(logger).info("Mémoire de l'image libérée.");
        verify(image).flush();
    }

    @Test
    void processTaskWithTimeout_ShouldComplete_WhenTaskCompletesWithinTimeout() {
        // Appel de la méthode à tester
        CompletableFuture<String> future = asyncTasks.processTaskWithTimeout();

        // Vérification que la tâche se termine dans le temps imparti
        assertDoesNotThrow(() -> future.get(6, TimeUnit.SECONDS));
    }

    @Test
    void processTaskWithTimeout_ShouldTimeout_WhenTaskExceedsTimeout() {
        // Appel de la méthode à tester
        CompletableFuture<String> future = asyncTasks.processTaskWithTimeout();

        // Vérification que la tâche lance une TimeoutException
        assertThrows(TimeoutException.class, () -> future.get(4, TimeUnit.SECONDS));
    }
}