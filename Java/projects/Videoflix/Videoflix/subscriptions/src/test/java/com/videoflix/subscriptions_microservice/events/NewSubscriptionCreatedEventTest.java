package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class NewSubscriptionCreatedEventTest {

    // Test pour vérifier la création de l'événement et la récupération des
    // informations de l'utilisateur et de l'abonnement
    @Test
    void newSubscriptionCreatedEvent_shouldHoldUserAndSubscriptionInformation() {
        // GIVEN : Création d'un objet source (peut être n'importe quel objet), d'un
        // utilisateur mocké et d'un abonnement mocké
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);

        // WHEN : Création d'une instance de NewSubscriptionCreatedEvent
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(source, mockUser, mockSubscription);

        // THEN : Vérification que l'événement a été correctement créé et contient les
        // informations attendues
        assertNotNull(event, "L'événement ne devrait pas être null.");
        assertEquals(source, event.getSource(), "La source de l'événement doit correspondre.");
        assertEquals(mockUser, event.getUser(), "L'utilisateur associé à l'événement doit correspondre.");
        assertEquals(mockSubscription, event.getSubscription(),
                "L'abonnement associé à l'événement doit correspondre.");
    }

    // Test pour vérifier que l'utilisateur associé à l'événement est correctement
    // récupéré
    @Test
    void getUser_shouldReturnTheCorrectUser() {
        // GIVEN : Création d'un utilisateur mocké
        User mockUser = mock(User.class);
        Object source = new Object();
        Subscription mockSubscription = mock(Subscription.class);
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getUser()
        User retrievedUser = event.getUser();

        // THEN : Vérification que la méthode retourne l'utilisateur correct
        assertEquals(mockUser, retrievedUser,
                "La méthode getUser() devrait retourner l'utilisateur associé à l'événement.");
    }

    // Test pour vérifier que l'abonnement associé à l'événement est correctement
    // récupéré
    @Test
    void getSubscription_shouldReturnTheCorrectSubscription() {
        // GIVEN : Création d'un abonnement mocké
        Subscription mockSubscription = mock(Subscription.class);
        Object source = new Object();
        User mockUser = mock(User.class);
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(source, mockUser, mockSubscription);

        // WHEN : Appel de la méthode getSubscription()
        Subscription retrievedSubscription = event.getSubscription();

        // THEN : Vérification que la méthode retourne l'abonnement correct
        assertEquals(mockSubscription, retrievedSubscription,
                "La méthode getSubscription() devrait retourner l'abonnement associé à l'événement.");
    }

    // Test pour vérifier que l'événement hérite bien de ApplicationEvent
    @Test
    void newSubscriptionCreatedEvent_isAnApplicationEvent() {
        // GIVEN : Création d'une instance de NewSubscriptionCreatedEvent
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        NewSubscriptionCreatedEvent event = new NewSubscriptionCreatedEvent(source, mockUser, mockSubscription);

        // THEN : Vérification que l'instance est une sous-classe de ApplicationEvent
        assertEquals(ApplicationEvent.class, event.getClass().getSuperclass(),
                "NewSubscriptionCreatedEvent devrait hériter de ApplicationEvent.");
    }
}