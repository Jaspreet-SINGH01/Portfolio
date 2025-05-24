package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.FailedEmail;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import com.videoflix.subscriptions_microservice.repositories.FailedEmailRepository;
import com.videoflix.subscriptions_microservice.services.AdminNotificationService;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator; // Nécessaire pour simuler @Retryable
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy; // Pour activer les aspects Spring (dont Retry)
import org.springframework.retry.annotation.EnableRetry; // Pour activer le support des retries
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith(SpringExtension.class) et @ContextConfiguration sont nécessaires pour que Spring Retry
// fonctionne correctement dans un test unitaire. Sans cela, les annotations @Retryable et @Recover
// ne seraient pas interprétées par Spring AOP.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WelcomeEmailTaskTest.TestConfig.class)
@DisplayName("Tests pour WelcomeEmailTask (avec Spring Retry)")
class WelcomeEmailTaskTest {

    // Définition d'une configuration Spring minimale pour activer Spring Retry.
    // Cela permet aux annotations @Retryable et @Recover d'être traitées.
    @Configuration
    @EnableRetry // Active le support des retries.
    @EnableAspectJAutoProxy(proxyTargetClass = true) // Active les proxy AOP pour les classes (nécessaire pour
                                                     // @Retryable).
    static class TestConfig {
        // Déclare la tâche à tester comme un bean Spring.
        // Spring va ensuite s'occuper d'y injecter les dépendances mockées.
        @Bean
        public WelcomeEmailTask welcomeEmailTask(NotificationService notificationService,
                FailedEmailRepository failedEmailRepository,
                AdminNotificationService adminNotificationService) {
            return new WelcomeEmailTask(notificationService, failedEmailRepository, adminNotificationService);
        }

        // Déclare un bean pour AnnotationAwareAspectJAutoProxyCreator qui est requis
        // pour que Spring Retry
        // et d'autres annotations AOP fonctionnent correctement.
        @Bean
        public static AnnotationAwareAspectJAutoProxyCreator annotationAwareAspectJAutoProxyCreator() {
            return new AnnotationAwareAspectJAutoProxyCreator();
        }
    }

    // @Autowired injecte l'instance réelle de la tâche qui a été traitée par Spring
    // AOP (avec Retry).
    @Autowired
    private WelcomeEmailTask welcomeEmailTask;

    // @Mock pour les dépendances de la tâche.
    @Mock
    private NotificationService notificationService;
    @Mock
    private AdminNotificationService adminNotificationService;
    @Mock
    private FailedEmailRepository failedEmailRepository;

    // Captors pour vérifier les arguments passés aux méthodes mockées.
    private ArgumentCaptor<FailedEmail> failedEmailCaptor;

    // Objets User et Subscription pour les tests.
    private User testUser;
    private Subscription testSubscription;
    private NewSubscriptionCreatedEvent testEvent;

    /**
     * Configuration initiale avant chaque test.
     */
    @BeforeEach
    void setUp() {
        // Initialisation des objets de test.
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstname("John");

        testSubscription = new Subscription();
        testSubscription.setId(101L);
        testSubscription.setUser(testUser);

        testEvent = new NewSubscriptionCreatedEvent(CALLS_REAL_METHODS, testUser, testSubscription);

        // Réinitialise les mocks pour garantir un état propre avant chaque test.
        reset(notificationService, adminNotificationService, failedEmailRepository);

        // Initialise les captors.
        failedEmailCaptor = ArgumentCaptor.forClass(FailedEmail.class);
    }

    @Test
    @DisplayName("Devrait envoyer un e-mail de bienvenue avec succès sans retry")
    void handleNewSubscriptionCreatedEvent_shouldSendEmailSuccessfullyWithoutRetry() {
        // GIVEN: Le service de notification envoie l'e-mail sans erreur.
        doNothing().when(notificationService).sendWelcomeEmail(any(User.class), any(Subscription.class));

        // WHEN: L'événement est traité par la tâche.
        welcomeEmailTask.handleNewSubscriptionCreatedEvent(testEvent);

        // THEN:
        // Vérifie que `sendWelcomeEmail` a été appelé une seule fois.
        verify(notificationService, times(1)).sendWelcomeEmail(testUser, testSubscription);
        // Vérifie que la méthode de recovery et les services associés n'ont jamais été
        // appelés.
        verify(failedEmailRepository, never()).save(any(FailedEmail.class));
        verify(adminNotificationService, never()).notifyAdminEmailSendFailure(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Devrait tenter d'envoyer l'e-mail plusieurs fois et réussir après quelques retries")
    void handleNewSubscriptionCreatedEvent_shouldRetryAndSucceed() {
        // GIVEN: Le service de notification échoue 2 fois, puis réussit à la 3ème
        // tentative.
        doThrow(new RuntimeException("Erreur simulée #1")) // 1ère tentative échoue
                .doThrow(new RuntimeException("Erreur simulée #2")) // 2ème tentative échoue
                .doNothing() // 3ème tentative réussit (maxAttempts = 3)
                .when(notificationService).sendWelcomeEmail(any(User.class), any(Subscription.class));

        // WHEN: L'événement est traité.
        welcomeEmailTask.handleNewSubscriptionCreatedEvent(testEvent);

        // THEN:
        // Vérifie que `sendWelcomeEmail` a été appelé 3 fois au total (2 échecs + 1
        // succès).
        verify(notificationService, times(3)).sendWelcomeEmail(testUser, testSubscription);
        // Vérifie que la méthode de recovery et les services associés n'ont jamais été
        // appelés car l'envoi a finalement réussi.
        verify(failedEmailRepository, never()).save(any(FailedEmail.class));
        verify(adminNotificationService, never()).notifyAdminEmailSendFailure(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Devrait échouer après toutes les tentatives et déclencher la méthode de recovery")
    void handleNewSubscriptionCreatedEvent_shouldFailAllRetriesAndRecover() {
        // GIVEN: Le service de notification échoue à toutes les tentatives (MAX_RETRIES
        // = 3).
        doThrow(new RuntimeException("Erreur persistante simulée"))
                .when(notificationService).sendWelcomeEmail(any(User.class), any(Subscription.class));

        // WHEN: L'événement est traité.
        welcomeEmailTask.handleNewSubscriptionCreatedEvent(testEvent);

        // THEN:
        // 1. Vérifie que `sendWelcomeEmail` a été appelé MAX_RETRIES (3) fois.
        verify(notificationService, times(WelcomeEmailTask.MAX_RETRIES))
                .sendWelcomeEmail(testUser, testSubscription);

        // 2. Vérifie que `failedEmailRepository.save` a été appelé une fois.
        verify(failedEmailRepository, times(1)).save(failedEmailCaptor.capture());
        FailedEmail capturedFailedEmail = failedEmailCaptor.getValue();
        assertNotNull(capturedFailedEmail);
        assertEquals(testUser.getEmail(), capturedFailedEmail.getRecipientEmail());
        assertEquals("Bienvenue chez Videoflix ! (Échec d'envoi)", capturedFailedEmail.getSubject());
        // Le corps de l'e-mail d'échec doit contenir des informations pertinentes.
        assertTrue(capturedFailedEmail.getBody().contains(testUser.getFirstname()));
        assertTrue(capturedFailedEmail.getBody().contains(String.valueOf(testUser.getId())));
        assertTrue(capturedFailedEmail.getBody().contains(String.valueOf(testSubscription.getId())));
        assertTrue(capturedFailedEmail.getBody().contains("Erreur persistante simulée"));
        assertEquals(WelcomeEmailTask.MAX_RETRIES, capturedFailedEmail.getAttemptCount());
        assertEquals("Erreur persistante simulée", capturedFailedEmail.getFailureReason());
        assertNotNull(capturedFailedEmail.getCreationTimestamp());

        // 3. Vérifie que `adminNotificationService.notifyAdminEmailSendFailure` a été
        // appelé une fois.
        verify(adminNotificationService, times(1)).notifyAdminEmailSendFailure(
                testUser, "Bienvenue", "Erreur persistante simulée");
    }

    @Test
    @DisplayName("Devrait gérer une erreur lors de l'enregistrement de l'e-mail échoué en base de données")
    void recovery_shouldHandleDbSaveError() {
        // GIVEN: Le service de notification échoue à toutes les tentatives.
        doThrow(new RuntimeException("Erreur persistante d'envoi"))
                .when(notificationService).sendWelcomeEmail(any(User.class), any(Subscription.class));

        // GIVEN: L'enregistrement de l'e-mail échoué en base de données échoue.
        doThrow(new RuntimeException("Erreur de sauvegarde DB simulée"))
                .when(failedEmailRepository).save(any(FailedEmail.class));

        // WHEN: L'événement est traité (ce qui mènera à la recovery).
        welcomeEmailTask.handleNewSubscriptionCreatedEvent(testEvent);

        // THEN:
        // 1. Vérifie que `sendWelcomeEmail` a été appelé MAX_RETRIES (3) fois.
        verify(notificationService, times(WelcomeEmailTask.MAX_RETRIES))
                .sendWelcomeEmail(testUser, testSubscription);
        // 2. Vérifie que `failedEmailRepository.save` a été appelé une fois (et a
        // échoué comme simulé).
        verify(failedEmailRepository, times(1)).save(any(FailedEmail.class));
        // 3. Vérifie que `adminNotificationService.notifyAdminEmailSendFailure` est
        // quand même appelé.
        // (La notification admin devrait être indépendante de l'échec de la DB).
        verify(adminNotificationService, times(1)).notifyAdminEmailSendFailure(
                testUser, "Bienvenue", "Erreur persistante d'envoi");
        // (On pourrait aussi vérifier les logs pour l'erreur de DB).
    }

    @Test
    @DisplayName("Devrait gérer une erreur lors de l'envoi de la notification d'échec à l'admin")
    void recovery_shouldHandleAdminNotificationError() {
        // GIVEN: Le service de notification échoue à toutes les tentatives.
        doThrow(new RuntimeException("Erreur persistante d'envoi"))
                .when(notificationService).sendWelcomeEmail(any(User.class), any(Subscription.class));

        // GIVEN: L'enregistrement de l'e-mail échoué en base de données réussit.
        when(failedEmailRepository.save(any(FailedEmail.class)))
                .thenReturn(new FailedEmail(null, null, null, 0, null, null));

        // GIVEN: L'envoi de la notification à l'admin échoue.
        doThrow(new RuntimeException("Erreur d'admin notification simulée"))
                .when(adminNotificationService).notifyAdminEmailSendFailure(any(), anyString(), anyString());

        // WHEN: L'événement est traité (ce qui mènera à la recovery).
        welcomeEmailTask.handleNewSubscriptionCreatedEvent(testEvent);

        // THEN:
        // 1. Vérifie que `sendWelcomeEmail` a été appelé MAX_RETRIES (3) fois.
        verify(notificationService, times(WelcomeEmailTask.MAX_RETRIES))
                .sendWelcomeEmail(testUser, testSubscription);
        // 2. Vérifie que `failedEmailRepository.save` a été appelé une fois et a
        // réussi.
        verify(failedEmailRepository, times(1)).save(any(FailedEmail.class));
        // 3. Vérifie que `adminNotificationService.notifyAdminEmailSendFailure` a été
        // appelé une fois (et a échoué).
        verify(adminNotificationService, times(1)).notifyAdminEmailSendFailure(
                testUser, "Bienvenue", "Erreur persistante d'envoi");
        // (On pourrait aussi vérifier les logs pour l'erreur d'admin notification).
    }
}