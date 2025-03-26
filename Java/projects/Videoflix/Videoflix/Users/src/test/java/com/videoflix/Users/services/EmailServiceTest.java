package com.videoflix.Users.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import com.videoflix.users_microservice.services.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests pour le service d'envoi d'emails.
 * 
 * Ces tests vérifient le comportement du service d'envoi d'emails,
 * en se concentrant sur l'envoi d'emails de bienvenue et la gestion
 * des différents scénarios possibles.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    // Mock du service d'envoi d'emails pour éviter les envois réels
    @Mock
    private JavaMailSender mailSender;

    // Mock du logger pour suivre les logs sans écriture réelle
    @Mock
    private Logger logger;

    // Capteur d'arguments pour intercepter les objets SimpleMailMessage
    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // Instance du service à tester
    private EmailService emailService;

    /**
     * Méthode de configuration exécutée avant chaque test.
     * Initialise le service d'email avec un mailSender mocké.
     */
    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    /**
     * Test de l'envoi d'un email de bienvenue avec succès.
     * 
     * Objectifs :
     * - Vérifier que l'email est envoyé correctement
     * - Confirmer les détails de l'email (destinataire, sujet, contenu)
     */
    @Test
    void sendWelcomeEmail_ShouldSendEmailSuccessfully() {
        // Préparation : Définition des données de test
        String testEmail = "test@example.com";
        String testUsername = "testuser";

        // Exécution : Envoi de l'email de bienvenue
        emailService.sendWelcomeEmail(testEmail, testUsername);

        // Vérification :
        // - Confirmation de l'appel à mailSender
        // - Validation des détails de l'email
        verify(mailSender, times(2)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(testEmail, capturedMessage.getTo()[0]);
        assertEquals("Bienvenue sur Videoflix !", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(testUsername));
    }

    /**
     * Test de gestion des exceptions lors de l'envoi d'email.
     * 
     * Objectifs :
     * - Vérifier que les exceptions sont gérées sans interrompre le processus
     * - Simuler une erreur d'envoi d'email
     */
    @Test
    void sendWelcomeEmail_ShouldHandleMailSendException() {
        // Préparation : Données de test et simulation d'exception
        String testEmail = "test@example.com";
        String testUsername = "testuser";

        // Simulation d'une exception lors de l'envoi
        doThrow(new MailSendException("Erreur de connexion SMTP"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        // Vérification : Aucune exception ne doit être levée
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testEmail, testUsername));
    }

    /**
     * Test de validation du contenu de l'email.
     * 
     * Objectifs :
     * - Vérifier que l'email contient les informations attendues
     * - Contrôler le format et le contenu du message
     */
    @Test
    void sendWelcomeEmail_ShouldCreateCorrectEmailContent() {
        // Préparation : Données de test
        String testEmail = "test@example.com";
        String testUsername = "testuser";

        // Exécution : Envoi de l'email
        emailService.sendWelcomeEmail(testEmail, testUsername);

        // Vérification multiple du contenu de l'email
        verify(mailSender, times(2)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        String emailText = capturedMessage.getText();

        // Vérifications groupées
        assertAll(
                () -> assertEquals(testEmail, capturedMessage.getTo()[0]),
                () -> assertEquals("Bienvenue sur Videoflix !", capturedMessage.getSubject()),
                () -> assertTrue(emailText.contains("Cher " + testUsername)),
                () -> assertTrue(emailText.contains("Bienvenue sur Videoflix !")));
    }

    /**
     * Test vérifiant le double appel de mailSender.send().
     * 
     * Note : Ce test met en évidence un comportement potentiellement
     * non intentionnel dans l'implémentation originale du service.
     */
    @Test
    void sendWelcomeEmail_ShouldSendEmailTwice() {
        // Préparation : Données de test
        String testEmail = "test@example.com";
        String testUsername = "testuser";

        // Exécution : Envoi de l'email
        emailService.sendWelcomeEmail(testEmail, testUsername);

        // Vérification : Confirmation de l'appel deux fois à send()
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    /**
     * Test de gestion des différents types d'exceptions.
     * 
     * Objectifs :
     * - Simuler et gérer différents types d'erreurs
     * - Vérifier la résilience du service
     */
    @Test
    void sendWelcomeEmail_ShouldHandleDifferentExceptions() {
        // Préparation : Données de test
        String testEmail = "test@example.com";
        String testUsername = "testuser";

        // Simulation de différents types d'exceptions
        doThrow(new RuntimeException("Erreur générique"))
                .doNothing()
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        // Vérification : Aucune exception ne doit être levée
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testEmail, testUsername));
    }
}