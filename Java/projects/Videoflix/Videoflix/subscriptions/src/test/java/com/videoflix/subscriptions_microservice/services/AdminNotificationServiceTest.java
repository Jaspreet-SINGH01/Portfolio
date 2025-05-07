package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class AdminNotificationServiceTest {

    // @Mock crée un mock de l'interface JavaMailSender.
    @Mock
    private JavaMailSender mailSender;

    // @Mock crée un mock de l'interface Logger.
    @Mock
    private Logger logger;

    // @InjectMocks crée une instance de AdminNotificationService et injecte les
    // mocks annotés avec @Mock.
    @InjectMocks
    private AdminNotificationService adminNotificationService;

    // @Captor permet de capturer les arguments passés à une méthode mockée.
    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // Test pour vérifier que la notification d'échec d'envoi d'e-mail est envoyée
    // correctement aux administrateurs.
    @Test
    void notifyAdminEmailSendFailure_shouldSendEmailToConfiguredAdmins() {
        // GIVEN : Un utilisateur, un type d'e-mail, un message d'erreur et une liste
        // d'e-mails d'administrateur configurée.
        User user = new User();
        user.setId(123L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");

        String emailType = "WELCOME_EMAIL";
        String errorMessage = "Failed to send email due to SMTP error.";
        List<String> adminEmails = Arrays.asList("admin1@example.com", "admin2@example.com");
        String notificationSender = "noreply@videoflix.com";

        // Injection des dépendances avec ReflectionTestUtils.
        ReflectionTestUtils.setField(adminNotificationService, "adminEmails", adminEmails);
        ReflectionTestUtils.setField(adminNotificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(adminNotificationService, "logger", logger);

        // WHEN : Appel de la méthode à tester.
        adminNotificationService.notifyAdminEmailSendFailure(user, emailType, errorMessage);

        // THEN : Capture du message envoyé.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Vérification des destinataires.
        assertEquals(adminEmails.size(), sentMessage.getTo().length);
        assertTrue(adminEmails.containsAll(List.of(sentMessage.getTo())));

        // Vérification de l'expéditeur.
        assertEquals(notificationSender, sentMessage.getFrom());

        // Vérification du sujet.
        assertEquals("[ALERTE] Échec d'envoi d'e-mail utilisateur", sentMessage.getSubject());

        // Vérification du corps du message.
        String expectedText = String.format(
                """
                        L'envoi de l'e-mail de type '%s' à l'utilisateur %s (ID: %d, Email: %s) a échoué après plusieurs tentatives.

                        Message d'erreur : %s

                        Veuillez vérifier les logs pour plus de détails.""",
                emailType,
                user.getFirstname() + " " + user.getLastname(),
                user.getId(),
                user.getEmail(),
                errorMessage);
        assertEquals(expectedText, sentMessage.getText());

        // Vérification que le logger a bien été appelé.
        verify(logger, times(1)).info(
                "Notification d'échec d'envoi d'e-mail envoyée à l'équipe d'administration pour l'utilisateur {}",
                user.getId());
    }

    // Test pour vérifier que rien n'est envoyé si la liste des e-mails
    // d'administrateur est vide.
    @Test
    void notifyAdminEmailSendFailure_shouldNotSendEmailIfAdminEmailsEmpty() {
        // GIVEN : Un utilisateur, un type d'e-mail, un message d'erreur et une liste
        // d'e-mails d'administrateur vide.
        User user = new User();
        String emailType = "PASSWORD_RESET";
        String errorMessage = "Connection timed out.";
        List<String> adminEmails = Collections.emptyList();
        String notificationSender = "noreply@videoflix.com";

        ReflectionTestUtils.setField(adminNotificationService, "adminEmails", adminEmails);
        ReflectionTestUtils.setField(adminNotificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(adminNotificationService, "logger", logger); // Injecter le mock du logger

        // WHEN : Appel de la méthode notifyAdminEmailSendFailure.
        adminNotificationService.notifyAdminEmailSendFailure(user, emailType, errorMessage);

        // THEN : Vérification que la méthode send du mailSender n'a jamais été appelée.
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        // Vérification que le logger warn a été appelé pour indiquer l'absence
        // d'adresses e-mail d'administrateur.
        verify(logger, times(1)).warn(
                "Aucune adresse e-mail d'administrateur configurée pour les notifications d'échec d'envoi d'e-mail.");
    }

    // Test pour vérifier la gestion d'une exception lors de l'envoi de l'e-mail à
    // l'administrateur.
    @Test
    void notifyAdminEmailSendFailure_shouldLogErrorIfMailSendFails() {
        // GIVEN : Un utilisateur, un type d'e-mail, un message d'erreur et une liste
        // d'e-mails d'administrateur configurée.
        User user = new User();
        user.getId();
        String emailType = "SUBSCRIPTION_RENEWAL_FAILED";
        String errorMessage = "Authentication required.";
        List<String> adminEmails = Collections.singletonList("admin@example.com");
        String notificationSender = "noreply@videoflix.com";
        Exception mailException = new RuntimeException("Failed to send mail");

        ReflectionTestUtils.setField(adminNotificationService, "adminEmails", adminEmails);
        ReflectionTestUtils.setField(adminNotificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(adminNotificationService, "logger", logger); // Injecter le mock du logger

        // Configuration du mock mailSender pour lancer une exception lors de l'envoi.
        doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

        // WHEN : Appel de la méthode notifyAdminEmailSendFailure.
        adminNotificationService.notifyAdminEmailSendFailure(user, emailType, errorMessage);

        // THEN : Vérification que la méthode send du mailSender a été appelée une fois.
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        // Vérification que le logger error a été appelé pour enregistrer l'erreur
        // d'envoi.
        verify(logger, times(1)).error(
                "Erreur lors de l'envoi de la notification d'échec d'e-mail à l'équipe d'administration pour l'utilisateur {} : {}",
                user.getId(), mailException);
    }

    // Test pour vérifier que rien ne se passe si la liste des e-mails
    // d'administrateur est nulle.
    @Test
    void notifyAdminEmailSendFailure_shouldNotSendEmailIfAdminEmailsIsNull() {
        // GIVEN : Un utilisateur, un type d'e-mail, un message d'erreur et une liste
        // d'e-mails d'administrateur nulle.
        User user = new User();
        String emailType = "ACCOUNT_DELETION";
        String errorMessage = "Internal server error.";
        List<String> adminEmails = null;
        String notificationSender = "noreply@videoflix.com";

        ReflectionTestUtils.setField(adminNotificationService, "adminEmails", adminEmails);
        ReflectionTestUtils.setField(adminNotificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(adminNotificationService, "logger", logger); // Injecter le mock du logger

        // WHEN : Appel de la méthode notifyAdminEmailSendFailure.
        adminNotificationService.notifyAdminEmailSendFailure(user, emailType, errorMessage);

        // THEN : Vérification que la méthode send du mailSender n'a jamais été appelée.
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        // Vérification que le logger warn a été appelé pour indiquer l'absence
        // d'adresses e-mail d'administrateur.
        verify(logger, times(1)).warn(
                "Aucune adresse e-mail d'administrateur configurée pour les notifications d'échec d'envoi d'e-mail.");
    }
}