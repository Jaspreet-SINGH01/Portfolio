package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PaymentInfoUpdatedEventTest {

    // Test pour vérifier la création de l'événement avec les informations de base
    // (source, utilisateur, abonnement)
    @Test
    void paymentInfoUpdatedEvent_shouldHoldBasicInformation() {
        // GIVEN : Création d'un objet source, d'un utilisateur mocké et d'un abonnement
        // mocké
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);

        // WHEN : Création d'une instance de PaymentInfoUpdatedEvent avec le
        // constructeur de base
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // THEN : Vérification que l'événement est correctement créé et contient les
        // informations de base
        assertNotNull(event, "L'événement ne devrait pas être null.");
        assertEquals(source, event.getSource(), "La source de l'événement doit correspondre.");
        assertEquals(mockUser, event.getUser(), "L'utilisateur associé à l'événement doit correspondre.");
        assertEquals(mockSubscription, event.getSubscription(),
                "L'abonnement associé à l'événement doit correspondre.");
        assertEquals("", event.getUpdatedPaymentMethod(),
                "La méthode de paiement mise à jour devrait être initialisée à une chaîne vide.");
        assertEquals("", event.getLastFourDigits(),
                "Les quatre derniers chiffres devraient être initialisés à une chaîne vide.");
    }

    // Test pour vérifier la récupération de l'utilisateur associé à l'événement
    @Test
    void getUser_shouldReturnTheCorrectUser() {
        // GIVEN : Création d'un utilisateur mocké
        User mockUser = mock(User.class);
        Object source = new Object();
        Subscription mockSubscription = mock(Subscription.class);
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getUser()
        User retrievedUser = event.getUser();

        // THEN : Vérification que la méthode retourne l'utilisateur correct
        assertEquals(mockUser, retrievedUser,
                "La méthode getUser() devrait retourner l'utilisateur associé à l'événement.");
    }

    // Test pour vérifier la récupération de l'abonnement associé à l'événement
    @Test
    void getSubscription_shouldReturnTheCorrectSubscription() {
        // GIVEN : Création d'un abonnement mocké
        Subscription mockSubscription = mock(Subscription.class);
        Object source = new Object();
        User mockUser = mock(User.class);
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getSubscription()
        Subscription retrievedSubscription = event.getSubscription();

        // THEN : Vérification que la méthode retourne l'abonnement correct
        assertEquals(mockSubscription, retrievedSubscription,
                "La méthode getSubscription() devrait retourner l'abonnement associé à l'événement.");
    }

    // Test pour vérifier la récupération de la méthode de paiement mise à jour
    // (initialisée à vide dans le constructeur actuel)
    @Test
    void getUpdatedPaymentMethod_shouldReturnEmptyStringByDefaultConstructor() {
        // GIVEN : Création d'un événement avec le constructeur par défaut
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getUpdatedPaymentMethod()
        String updatedPaymentMethod = event.getUpdatedPaymentMethod();

        // THEN : Vérification que la méthode retourne la chaîne vide initiale
        assertEquals("", updatedPaymentMethod,
                "La méthode getUpdatedPaymentMethod() devrait retourner une chaîne vide par défaut.");
    }

    // Test pour vérifier la récupération des quatre derniers chiffres (initialisés
    // à vide dans le constructeur actuel)
    @Test
    void getLastFourDigits_shouldReturnEmptyStringByDefaultConstructor() {
        // GIVEN : Création d'un événement avec le constructeur par défaut
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getLastFourDigits()
        String lastFourDigits = event.getLastFourDigits();

        // THEN : Vérification que la méthode retourne la chaîne vide initiale
        assertEquals("", lastFourDigits,
                "La méthode getLastFourDigits() devrait retourner une chaîne vide par défaut.");
    }

    // Test pour vérifier que l'événement hérite bien de ApplicationEvent
    @Test
    void paymentInfoUpdatedEvent_isAnApplicationEvent() {
        // GIVEN : Création d'une instance de PaymentInfoUpdatedEvent
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        PaymentInfoUpdatedEvent event = new PaymentInfoUpdatedEvent(source, mockUser, mockSubscription);

        // THEN : Vérification que l'instance est une sous-classe de ApplicationEvent
        assertEquals(ApplicationEvent.class, event.getClass().getSuperclass(),
                "PaymentInfoUpdatedEvent devrait hériter de ApplicationEvent.");
    }
}