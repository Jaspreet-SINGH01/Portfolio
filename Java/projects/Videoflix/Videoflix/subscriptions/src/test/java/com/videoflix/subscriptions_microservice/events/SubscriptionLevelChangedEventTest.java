package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SubscriptionLevelChangedEventTest {

    // Test pour vérifier la création de l'événement et la récupération de toutes
    // les informations
    @Test
    void subscriptionLevelChangedEvent_shouldHoldAllInformation() {
        // GIVEN : Création d'un objet source, d'un utilisateur mocké, d'un abonnement
        // mocké, et des anciens et nouveaux niveaux
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        String oldLevel = "BASIC";
        String newLevel = "PREMIUM";

        // WHEN : Création d'une instance de SubscriptionLevelChangedEvent
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

        // THEN : Vérification que l'événement contient toutes les informations
        // attendues
        assertNotNull(event, "L'événement ne devrait pas être null.");
        assertEquals(source, event.getSource(), "La source de l'événement doit correspondre.");
        assertEquals(mockUser, event.getUser(), "L'utilisateur associé à l'événement doit correspondre.");
        assertEquals(mockSubscription, event.getSubscription(),
                "L'abonnement associé à l'événement doit correspondre.");
        assertEquals(oldLevel, event.getOldLevel(), "L'ancien niveau d'abonnement doit correspondre.");
        assertEquals(newLevel, event.getNewLevel(), "Le nouveau niveau d'abonnement doit correspondre.");
    }

    // Test pour vérifier la récupération de l'utilisateur associé à l'événement
    @Test
    void getUser_shouldReturnTheCorrectUser() {
        // GIVEN : Création d'un utilisateur mocké
        User mockUser = mock(User.class);
        Object source = new Object();
        Subscription mockSubscription = mock(Subscription.class);
        String oldLevel = "BASIC";
        String newLevel = "PREMIUM";
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

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
        String oldLevel = "BASIC";
        String newLevel = "PREMIUM";
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

        // WHEN : Appel de la méthode getSubscription()
        Subscription retrievedSubscription = event.getSubscription();

        // THEN : Vérification que la méthode retourne l'abonnement correct
        assertEquals(mockSubscription, retrievedSubscription,
                "La méthode getSubscription() devrait retourner l'abonnement associé à l'événement.");
    }

    // Test pour vérifier la récupération de l'ancien niveau d'abonnement
    @Test
    void getOldLevel_shouldReturnTheCorrectOldLevel() {
        // GIVEN : Définition d'un ancien niveau d'abonnement
        String oldLevel = "BASIC";
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        String newLevel = "PREMIUM";
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

        // WHEN : Appel de la méthode getOldLevel()
        String retrievedOldLevel = event.getOldLevel();

        // THEN : Vérification que la méthode retourne l'ancien niveau correct
        assertEquals(oldLevel, retrievedOldLevel,
                "La méthode getOldLevel() devrait retourner l'ancien niveau d'abonnement.");
    }

    // Test pour vérifier la récupération du nouveau niveau d'abonnement
    @Test
    void getNewLevel_shouldReturnTheCorrectNewLevel() {
        // GIVEN : Définition d'un nouveau niveau d'abonnement
        String newLevel = "PREMIUM";
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        String oldLevel = "BASIC";
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

        // WHEN : Appel de la méthode getNewLevel()
        String retrievedNewLevel = event.getNewLevel();

        // THEN : Vérification que la méthode retourne le nouveau niveau correct
        assertEquals(newLevel, retrievedNewLevel,
                "La méthode getNewLevel() devrait retourner le nouveau niveau d'abonnement.");
    }

    // Test pour vérifier que l'événement hérite bien de ApplicationEvent
    @Test
    void subscriptionLevelChangedEvent_isAnApplicationEvent() {
        // GIVEN : Création d'une instance de SubscriptionLevelChangedEvent
        Object source = new Object();
        User mockUser = mock(User.class);
        Subscription mockSubscription = mock(Subscription.class);
        String oldLevel = "BASIC";
        String newLevel = "PREMIUM";
        SubscriptionLevelChangedEvent event = new SubscriptionLevelChangedEvent(source, mockUser, mockSubscription,
                oldLevel, newLevel);

        // THEN : Vérification que l'instance est une sous-classe de ApplicationEvent
        assertEquals(ApplicationEvent.class, event.getClass().getSuperclass(),
                "SubscriptionLevelChangedEvent devrait hériter de ApplicationEvent.");
    }
}