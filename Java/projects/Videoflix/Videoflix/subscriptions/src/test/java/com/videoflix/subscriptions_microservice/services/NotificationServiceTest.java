package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.templates.EmailTemplates;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    // @Mock crée un mock de l'interface JavaMailSender.
    @Mock
    private JavaMailSender mailSender;

    // @Mock crée un mock de l'interface Logger.
    @Mock
    private Logger logger;

    // @InjectMocks crée une instance de NotificationService et injecte les mocks.
    @InjectMocks
    private NotificationService notificationService;

    // @Captor pour capturer l'objet SimpleMailMessage envoyé par mailSender.
    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // Méthode utilitaire pour créer un utilisateur de test.
    private User createUser(Long id, String firstname, String lastname, String email) {
        User user = new User();
        user.setId(id);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        return user;
    }

    // Méthode utilitaire pour créer un abonnement de test.
    private Subscription createSubscription(Long id, SubscriptionLevel level, LocalDateTime endDate,
            LocalDateTime trialEndDate, LocalDateTime nextBillingDate) {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setSubscriptionLevel(level);
        subscription.setEndDate(endDate);
        subscription.setTrialEndDate(trialEndDate);
        subscription.setNextBillingDate(nextBillingDate);
        return subscription;
    }

    // Méthode utilitaire pour créer un SubscriptionLevel de test.
    private SubscriptionLevel createSubscriptionLevel(String features) {
        SubscriptionLevel level = new SubscriptionLevel();
        level.setFeatures(features); // Correction : utiliser le nom fourni
        return level;
    }

    // Test pour vérifier l'envoi de la notification d'expiration d'abonnement.
    @Test
    void sendSubscriptionExpiringNotification_shouldSendEmailWithCorrectContent() {
        // GIVEN : Un utilisateur et un abonnement valides.
        User user = createUser(1L, "John", "Doe", "john.doe@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Premium");
        LocalDateTime endDate = LocalDateTime.of(2025, 5, 10, 12, 0);
        Subscription subscription = createSubscription(101L, level, endDate, null, null);

        // Injection des dépendances nécessaires via ReflectionTestUtils
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        // WHEN : Appel de la méthode à tester
        notificationService.sendSubscriptionExpiringNotification(user, subscription);

        // THEN : Vérification du message envoyé
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Vérification du destinataire
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);

        // Vérification du sujet
        assertEquals(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRING, sentMessage.getSubject());

        // Vérification du contenu de l'e-mail
        String expectedContent = String.format(
                EmailTemplates.SUBSCRIPTION_EXPIRING_EMAIL,
                user.getFirstname(),
                level.getLevel(),
                endDate);
        assertEquals(expectedContent, sentMessage.getText());

        // Vérification de l'expéditeur
        assertEquals(notificationSender, sentMessage.getFrom());

        // Vérification du logging
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "expiration imminente", user.getId());
    }

    // Test pour vérifier que la notification d'expiration n'est pas envoyée si
    // l'utilisateur ou l'abonnement est null.
    @Test
    void sendSubscriptionExpiringNotification_shouldNotSendEmailIfUserOrSubscriptionIsNull() {
        // GIVEN : Un utilisateur null et un abonnement valide.
        User nullUser = null;
        Subscription subscription = createSubscription(101L, createSubscriptionLevel("Basic"),
                LocalDateTime.now().plusDays(5), null, null);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        // WHEN : Appel de la méthode avec un utilisateur null.
        notificationService.sendSubscriptionExpiringNotification(nullUser, subscription);

        // THEN : Vérification que l'e-mail n'a pas été envoyé et qu'une erreur a été
        // loguée.
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(logger, times(1)).error(NotificationService.NULL_PARAMS_ERROR);

        // GIVEN : Un utilisateur valide et un abonnement null.
        User user = createUser(2L, "Jane", "Doe", "jane.doe@example.com");
        Subscription nullSubscription = null;

        // WHEN : Appel de la méthode avec un abonnement null.
        notificationService.sendSubscriptionExpiringNotification(user, nullSubscription);

        // THEN : Vérification que l'e-mail n'a pas été envoyé et qu'une erreur a été
        // loguée.
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(logger, times(2)).error(NotificationService.NULL_PARAMS_ERROR);
    }

    // Test pour vérifier que la notification d'expiration n'est pas envoyée si
    // l'e-mail de l'utilisateur est manquant.
    @Test
    void sendSubscriptionExpiringNotification_shouldNotSendEmailIfUserEmailIsMissing() {
        // GIVEN : Un utilisateur sans adresse e-mail et un abonnement valide.
        User userWithoutEmail = createUser(3L, "No", "Email", null);
        Subscription subscription = createSubscription(102L, createSubscriptionLevel("Standard"),
                LocalDateTime.now().plusDays(3), null, null);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        // WHEN : Appel de la méthode.
        notificationService.sendSubscriptionExpiringNotification(userWithoutEmail, subscription);

        // THEN : Vérification que l'e-mail n'a pas été envoyé et qu'un avertissement a
        // été logué.
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(logger, times(1)).warn(
                "Impossible d'envoyer l'e-mail à l'utilisateur {} car l'adresse e-mail est manquante.",
                userWithoutEmail.getId());
    }

    // Répétez des tests similaires pour chaque méthode d'envoi d'e-mail
    // (sendSubscriptionExpiredNotification,
    // sendTrialPeriodEndingNotification, sendTrialPeriodEndedNotification,
    // sendWelcomeEmail,
    // sendPaymentReminderNotification) en vérifiant :
    // 1. L'envoi correct de l'e-mail avec le bon contenu basé sur les entrées.
    // 2. La non-exécution et le logging d'erreur si l'utilisateur ou l'abonnement
    // est null.
    // 3. La non-exécution et le logging d'avertissement si l'adresse e-mail de
    // l'utilisateur est manquante.

    @Test
    void sendSubscriptionExpiredNotification_shouldSendEmailWithCorrectContent() {
        User user = createUser(4L, "Alice", "Smith", "alice.smith@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Premium");
        LocalDateTime endDate = LocalDateTime.of(2025, 5, 1, 10, 0);
        Subscription subscription = createSubscription(103L, level, endDate, null, null);
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        notificationService.sendSubscriptionExpiredNotification(user, subscription);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRED, sentMessage.getSubject());
        assertEquals(String.format(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRED, user.getFirstname(), level, endDate),
                sentMessage.getText());
        assertEquals(notificationSender, sentMessage.getFrom());
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "expiration", user.getId());
    }

    @Test
    void sendTrialPeriodEndingNotification_shouldSendEmailWithCorrectContent() {
        User user = createUser(5L, "Bob", "Johnson", "bob.johnson@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Trial");
        LocalDateTime trialEndDate = LocalDateTime.of(2025, 5, 8, 18, 0);
        Subscription subscription = createSubscription(104L, level, null, trialEndDate, null);
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        notificationService.sendTrialPeriodEndingNotification(user, subscription);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(EmailTemplates.SUBJECT_TRIAL_ENDING, sentMessage.getSubject());
        assertEquals(String.format(EmailTemplates.TRIAL_PERIOD_ENDING_NOTIFICATION, user.getFirstname(), level,
                trialEndDate), sentMessage.getText());
        assertEquals(notificationSender, sentMessage.getFrom());
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "fin de période d’essai imminente",
                user.getId());
    }

    @Test
    void sendTrialPeriodEndedNotification_shouldSendEmailWithCorrectContent() {
        User user = createUser(6L, "Charlie", "Brown", "charlie.brown@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Trial");
        LocalDateTime trialEndDate = LocalDateTime.of(2025, 5, 5, 0, 0);
        Subscription subscription = createSubscription(105L, level, null, trialEndDate, null);
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        notificationService.sendTrialPeriodEndedNotification(user, subscription);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(EmailTemplates.SUBJECT_TRIAL_ENDED, sentMessage.getSubject());
        assertEquals(String.format(EmailTemplates.SUBJECT_TRIAL_ENDED, user.getFirstname(), level, trialEndDate),
                sentMessage.getText());
        assertEquals(notificationSender, sentMessage.getFrom());
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "fin de période d’essai", user.getId());
    }

    @Test
    void sendWelcomeEmail_shouldSendEmailWithCorrectContent() {
        User user = createUser(7L, "Diana", "Prince", "diana.prince@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Super");
        Subscription subscription = createSubscription(106L, level, null, null, null);
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        notificationService.sendWelcomeEmail(user, subscription);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(EmailTemplates.SUBJECT_WELCOME, sentMessage.getSubject());
        assertEquals(String.format(EmailTemplates.WELCOME_EMAIL, user.getFirstname(), level), sentMessage.getText());
        assertEquals(notificationSender, sentMessage.getFrom());
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "de bienvenue", user.getId());
    }

    @Test
    void sendPaymentReminderNotification_shouldSendEmailWithCorrectContent() {
        User user = createUser(8L, "Eve", "Harlow", "eve.harlow@example.com");
        SubscriptionLevel level = createSubscriptionLevel("Pro");
        LocalDateTime nextBillingDate = LocalDateTime.of(2025, 5, 15, 9, 0);
        Subscription subscription = createSubscription(107L, level, null, null, nextBillingDate);
        int daysBefore = 3;
        String expectedBody = String.format(EmailTemplates.SUBSCRIPTION_NOTIFICATION, user.getFirstname(), level,
                nextBillingDate, daysBefore);
        String notificationSender = "noreply@videoflix.com";
        ReflectionTestUtils.setField(notificationService, "notificationSender", notificationSender);
        ReflectionTestUtils.setField(notificationService, "logger", logger);

        notificationService.sendPaymentReminderNotification(user, subscription, daysBefore);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(EmailTemplates.SUBJECT_PAYMENT_REMINDER, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
        assertEquals(notificationSender, sentMessage.getFrom());
        verify(logger, times(1)).info("E-mail {} envoyé à l'utilisateur {}", "de rappel de paiement", user.getId());
    }
}