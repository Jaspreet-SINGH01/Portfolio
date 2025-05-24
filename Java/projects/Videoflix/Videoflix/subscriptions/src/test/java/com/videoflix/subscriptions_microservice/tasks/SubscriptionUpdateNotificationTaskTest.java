package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.events.PaymentInfoUpdatedEvent;
import com.videoflix.subscriptions_microservice.events.SubscriptionLevelChangedEvent;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import com.videoflix.subscriptions_microservice.services.NotificationService;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour SubscriptionUpdateNotificationTask")
class SubscriptionUpdateNotificationTaskTest {

    // @Mock pour les dépendances que la tâche utilise, mais que nous n'avons pas
    // besoin de tester directement ici.
    // Les annotations @SuppressWarnings("unused") dans la classe testée indiquent
    // qu'ils sont injectés
    // mais pas utilisés par les méthodes de l'écouteur, ce qui est inhabituel et
    // pourrait indiquer
    // un code mort ou une conception à revoir. Pour les tests, nous les mockons
    // quand même.
    @Mock
    private NotificationService notificationService;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;

    // @Mock pour EmailService. C'est le service principal que nous voulons
    // vérifier.
    // Puisque EmailService est instancié *dans* le constructeur de la tâche,
    // nous devons mocker JavaMailSender et injecter notre propre instance de
    // EmailService.
    @Mock
    private JavaMailSender mailSender; // Mock du mailSender pour construire EmailService

    // Nous devons instancier manuellement la tâche car EmailService est créé dans
    // le constructeur.
    // Nous ne pouvons pas utiliser @InjectMocks directement sur la tâche si une
    // dépendance est instanciée à l'intérieur.
    private SubscriptionUpdateNotificationTask subscriptionUpdateNotificationTask;

    // ArgumentCaptor pour capturer l'e-mail envoyé, le sujet et le corps.
    @SuppressWarnings("unused")
    private ArgumentCaptor<String> recipientCaptor;
    @SuppressWarnings("unused")
    private ArgumentCaptor<String> subjectCaptor;
    @SuppressWarnings("unused")
    private ArgumentCaptor<String> bodyCaptor;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        // Réinitialise les mocks.
        reset(notificationService, subscriptionRepository, userRepository, mailSender);

        // Crée une instance réelle d'EmailService avec le mock de JavaMailSender.
        // Puisque EmailService est `final`, nous devons le mocker directement.
        // On doit simuler le comportement du constructeur de
        // `SubscriptionUpdateNotificationTask`.
        // C'est un cas particulier dû à l'instanciation de `EmailService` dans le
        // constructeur de la tâche.
        // Normalement, `EmailService` serait un `@Component` et directement `@Mock` ou
        // `@InjectMocks`.

        // Plutôt que de mocker `EmailService` si c'est instancié dans le constructeur,
        // nous allons mocker la dépendance de `EmailService` (`JavaMailSender`).
        // Puis nous créons une vraie instance de `EmailService` pour s'assurer qu'il
        // fonctionne comme prévu.

        // Puis nous instancions la tâche, en lui passant les mocks et la vraie instance
        // de EmailService.
        // Cela nous permet de tester le comportement de la tâche qui utilise
        // EmailService.
        subscriptionUpdateNotificationTask = new SubscriptionUpdateNotificationTask(
                notificationService, subscriptionRepository, userRepository, mailSender);

        // Capture les arguments passés à la méthode `sendSubscriptionNotification` de
        // EmailService.
        recipientCaptor = ArgumentCaptor.forClass(String.class);
        subjectCaptor = ArgumentCaptor.forClass(String.class);
        bodyCaptor = ArgumentCaptor.forClass(String.class);

        // --- Ajout configuration ListAppender pour la capture des logs ---
        Logger logger = (Logger) LoggerFactory.getLogger(SubscriptionUpdateNotificationTask.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    @DisplayName("Devrait envoyer une notification de changement de niveau d'abonnement")
    void handleSubscriptionLevelChangedEvent_shouldSendNotification() {
        // GIVEN: Un événement de changement de niveau d'abonnement.
        User user = new User();
        user.setId(1L);
        user.setFirstname("Jean");
        user.setEmail("jean.dupont@example.com");

        Subscription subscription = new Subscription();
        subscription.setId(101L);
        // Correction : on crée un objet SubscriptionLevel et on utilise l'enum interne
        // Level
        SubscriptionLevel level = new SubscriptionLevel();
        level.setLevel(SubscriptionLevel.Level.PREMIUM);
        subscription.setSubscriptionLevel(level);

        String oldLevel = SubscriptionLevel.Level.BASIC.name(); // Niveau précédent

        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(oldLevel, user, subscription, oldLevel,
                oldLevel);

        // WHEN: L'événement est publié et traité par la tâche.
        subscriptionUpdateNotificationTask.handleSubscriptionLevelChangedEvent(event);

        // THEN:
        // Vérifie que la méthode `sendSubscriptionNotification` de l'EmailService a été
        // appelée une fois.
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(mimeMessageCaptor.capture());
        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        try {
            String content = (String) sentMessage.getContent();
            assertTrue(content.contains("changement de niveau"),
                    "Le corps de l'email doit mentionner le changement de niveau");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du contenu du MimeMessage", e);
        }

        // On peut également vérifier les logs si on a la configuration pour les logs.
        List<String> logMessages = listAppender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .toList();
        assertTrue(logMessages
                .contains("Notification de changement de niveau d'abonnement envoyée à l'utilisateur " + user.getId()));
    }

    @Test
    @DisplayName("Devrait envoyer une notification de mise à jour des informations de paiement")
    void handlePaymentInfoUpdatedEvent_shouldSendNotification() {
        // GIVEN: Un événement de mise à jour des informations de paiement.
        User user = new User();
        user.setId(2L);
        user.setFirstname("Alice");
        user.setEmail("alice.martin@example.com");

        Subscription subscription = new Subscription();
        subscription.setId(201L);

        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(subscription, user, subscription);

        // WHEN: L'événement est publié et traité par la tâche.
        subscriptionUpdateNotificationTask.handlePaymentInfoUpdatedEvent(event);

        // THEN:
        // Vérifie que `mailSender.send` a été appelé.
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Comme pour le test précédent, une vérification plus fine du contenu de
        // l'e-mail
        // nécessiterait un mock de `EmailService` injecté.
    }

    @Test
    @DisplayName("Devrait gérer les exceptions lors de l'envoi de la notification de changement de niveau")
    void handleSubscriptionLevelChangedEvent_shouldLogErrorOnException() throws Exception {
        // GIVEN: Une exception se produit lors de l'envoi de l'e-mail.
        User user = new User();
        user.setId(3L);
        user.setFirstname("Bob");
        user.setEmail("bob.brown@example.com");

        Subscription subscription = new Subscription();
        subscription.setId(301L);
        subscription.setSubscriptionLevel((SubscriptionLevel) Subscription.class.getField("BASIC").get(null));

        String oldLevel = Subscription.class.getField("BASIC").get(null).toString();

        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(oldLevel, user, subscription, oldLevel,
                oldLevel);

        // Simule une exception lorsque `mailSender.send` est appelé.
        doThrow(new RuntimeException("Erreur d'envoi d'e-mail simulée"))
                .when(mailSender).send(any(MimeMessage.class));

        // Prépare le ListAppender pour capturer les logs
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(SubscriptionUpdateNotificationTask.class);
        ListAppender<ILoggingEvent> localListAppender = new ListAppender<>();
        localListAppender.start();
        logger.addAppender(localListAppender);

        // WHEN: L'événement est traité.
        subscriptionUpdateNotificationTask.handleSubscriptionLevelChangedEvent(event);

        // THEN:
        // Vérifie que `mailSender.send` a bien été appelé (et a échoué comme simulé).
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Vérifie que l'erreur a été loguée
        List<String> logMessages = localListAppender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .toList();
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains(
                "Erreur lors de l'envoi de la notification de changement de niveau d'abonnement à l'utilisateur 3")));
    }

    @Test
    @DisplayName("Devrait gérer les exceptions lors de l'envoi de la notification de mise à jour de paiement")
    void handlePaymentInfoUpdatedEvent_shouldLogErrorOnException() throws Exception {
        // GIVEN: Une exception se produit lors de l'envoi de l'e-mail pour la mise à
        // jour de paiement.
        User user = new User();
        user.setId(4L);
        user.setFirstname("Carol");
        user.setEmail("carol.white@example.com");

        Subscription subscription = new Subscription();
        subscription.setId(401L);

        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(subscription, user, subscription);

        // Simule une exception lorsque `mailSender.send` est appelé.
        doThrow(new RuntimeException("Erreur d'envoi d'e-mail simulée pour paiement"))
                .when(mailSender).send(any(MimeMessage.class));

        // Prépare le ListAppender pour capturer les logs
        Logger logger = (Logger) LoggerFactory.getLogger(SubscriptionUpdateNotificationTask.class);
        ListAppender<ILoggingEvent> localListAppender = new ListAppender<>();
        localListAppender.start();
        logger.addAppender(localListAppender);

        // WHEN: L'événement est traité.
        subscriptionUpdateNotificationTask.handlePaymentInfoUpdatedEvent(event);

        // THEN:
        // Vérifie que `mailSender.send` a bien été appelé.
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Vérifie que l'erreur a été loguée.
        List<String> logMessages = localListAppender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .toList();
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains(
                "Erreur lors de l'envoi de la notification de mise à jour des informations de paiement à l'utilisateur 4")));
    }
}