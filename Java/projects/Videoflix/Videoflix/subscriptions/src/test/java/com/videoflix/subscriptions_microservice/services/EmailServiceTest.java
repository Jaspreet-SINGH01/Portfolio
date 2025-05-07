package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    // @Mock crée un mock de l'interface JavaMailSender.
    @Mock
    private JavaMailSender mailSender;

    // @InjectMocks crée une instance de EmailService et injecte le mock de
    // mailSender.
    @InjectMocks
    private EmailService emailService;

    // @Captor pour capturer l'objet SimpleMailMessage envoyé par mailSender.
    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // Test pour vérifier que sendSubscriptionNotification envoie un e-mail avec les
    // bons destinataire, sujet et corps.
    @Test
    void sendSubscriptionNotification_shouldSendEmailWithCorrectDetails() {
        // GIVEN : Un destinataire, un sujet et un corps d'e-mail.
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // WHEN : Appel de la méthode sendSubscriptionNotification.
        emailService.sendSubscriptionNotification(to, subject, body);

        // THEN : Vérification que la méthode send du mailSender a été appelée une fois
        // avec le message capturé.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Vérification du destinataire.
        assertEquals(to, sentMessage.getTo()[0]);
        // Vérification du sujet.
        assertEquals(subject, sentMessage.getSubject());
        // Vérification du corps.
        assertEquals(body, sentMessage.getText());
    }

    // Test pour vérifier que sendSubscriptionRenewalNotification envoie un e-mail
    // de renouvellement correct.
    @Test
    void sendSubscriptionRenewalNotification_shouldSendRenewalEmail() {
        // GIVEN : Un destinataire et un niveau d'abonnement.
        String to = "user@example.com";
        String subscriptionLevel = "Premium";
        String expectedSubject = "Votre abonnement Premium a été renouvelé";
        String expectedBody = "Cher client,\n\nVotre abonnement Premium a été renouvelé avec succès.\n\nMerci de votre fidélité.";

        // WHEN : Appel de la méthode sendSubscriptionRenewalNotification.
        emailService.sendSubscriptionRenewalNotification(to, subscriptionLevel);

        // THEN : Vérification que la méthode send du mailSender a été appelée avec le
        // message capturé et que le contenu est correct.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }

    // Test pour vérifier que sendSubscriptionExpirationNotification envoie un
    // e-mail d'expiration correct.
    @Test
    void sendSubscriptionExpirationNotification_shouldSendExpirationEmail() {
        // GIVEN : Un destinataire et un niveau d'abonnement.
        String to = "subscriber@example.com";
        String subscriptionLevel = "Basic";
        String expectedSubject = "Votre abonnement Basic va expirer bientôt";
        String expectedBody = "Cher client,\n\nVotre abonnement Basic expirera le [Date d'expiration].\nPensez à le renouveler pour continuer à profiter de nos services.\n\nCordialement.";

        // WHEN : Appel de la méthode sendSubscriptionExpirationNotification.
        emailService.sendSubscriptionExpirationNotification(to, subscriptionLevel);

        // THEN : Vérification que la méthode send du mailSender a été appelée avec le
        // message capturé et que le contenu est correct.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }

    // Test pour vérifier que sendSubscriptionExpirationImminentNotification envoie
    // un e-mail d'expiration imminente correct.
    @Test
    void sendSubscriptionExpirationImminentNotification_shouldSendImminentExpirationEmail() {
        // GIVEN : Un destinataire, un niveau d'abonnement et une date d'expiration.
        String to = "premium.user@example.com";
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
        subscriptionLevel.setLevel(null);
        LocalDateTime expirationDate = LocalDateTime.of(2025, 5, 10, 10, 0);
        String formattedDate = "10/05/2025";
        String expectedSubject = "Votre abonnement Premium expire dans 3 jours !";
        String expectedBody = "Cher client,\n\nVotre abonnement Premium expire le " + formattedDate
                + ".\nNe manquez pas la fin de votre accès, renouvelez dès maintenant !\n\nCordialement.";

        // WHEN : Appel de la méthode sendSubscriptionExpirationImminentNotification.
        emailService.sendSubscriptionExpirationImminentNotification(to, subscriptionLevel, expirationDate);

        // THEN : Vérification que la méthode send du mailSender a été appelée avec le
        // message capturé et que le contenu est correct.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }

    // Test pour vérifier que sendPaymentFailedNotification envoie un e-mail d'échec
    // de paiement correct.
    @Test
    void sendPaymentFailedNotification_shouldSendPaymentFailedEmail() {
        // GIVEN : Un destinataire et un niveau d'abonnement.
        String to = "failed.payment@example.com";
        String subscriptionLevel = "Standard";
        String expectedSubject = "Échec du paiement de votre abonnement Standard";
        String expectedBody = "Cher client,\n\nLe paiement de votre abonnement Standard a échoué. Veuillez vérifier vos informations de paiement et réessayer.\n\nCordialement.";

        // WHEN : Appel de la méthode sendPaymentFailedNotification.
        emailService.sendPaymentFailedNotification(to, subscriptionLevel);

        // THEN : Vérification que la méthode send du mailSender a été appelée avec le
        // message capturé et que le contenu est correct.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }

    // Test pour vérifier que sendPaymentRetrySuccessNotification envoie un e-mail
    // de succès de paiement correct.
    @Test
    void sendPaymentRetrySuccessNotification_shouldSendPaymentSuccessEmail() {
        // GIVEN : Un destinataire et un niveau d'abonnement.
        String to = "success.payment@example.com";
        String subscriptionLevel = "Gold";
        String expectedSubject = "Paiement réussi pour votre abonnement Gold";
        String expectedBody = "Cher client,\n\nLe paiement de votre abonnement Gold a été effectué avec succès.\n\nMerci !";

        // WHEN : Appel de la méthode sendPaymentRetrySuccessNotification.
        emailService.sendPaymentRetrySuccessNotification(to, subscriptionLevel);

        // THEN : Vérification que la méthode send du mailSender a été appelée avec le
        // message capturé et que le contenu est correct.
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }
}