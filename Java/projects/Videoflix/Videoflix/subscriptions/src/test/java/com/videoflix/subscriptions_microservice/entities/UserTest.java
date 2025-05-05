package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    // Test pour vérifier la création et la récupération des attributs d'un
    // utilisateur
    @Test
    void user_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs pour les attributs de l'utilisateur
        String username = "testuser";
        String email = "test@example.com";
        String password = "securePassword";
        String firstname = "John";
        String lastname = "Doe";
        String pushToken = "pushToken123";
        String stripeCustomerId = "cus_abc456";

        // WHEN : Création d'une instance de User et définition de ses attributs
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPushToken(pushToken);
        user.setStripeCustomerId(stripeCustomerId);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(user.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(username, user.getUsername(), "Le nom d'utilisateur doit correspondre.");
        assertEquals(email, user.getEmail(), "L'e-mail doit correspondre.");
        assertEquals(password, user.getPassword(), "Le mot de passe doit correspondre.");
        assertEquals(firstname, user.getFirstname(), "Le prénom doit correspondre.");
        assertEquals(lastname, user.getLastname(), "Le nom de famille doit correspondre.");
        assertEquals(pushToken, user.getPushToken(), "Le push token doit correspondre.");
        assertEquals(stripeCustomerId, user.getStripeCustomerId(), "L'ID client Stripe doit correspondre.");
    }

    // Test pour vérifier que les attributs d'un utilisateur peuvent être modifiés
    @Test
    void user_shouldAllowAttributeModification() {
        // GIVEN : Création d'une instance de User et définition de valeurs initiales
        User user = new User();
        user.setUsername("olduser");
        user.setEmail("old@example.com");

        // WHEN : Modification des attributs de l'utilisateur
        String newUsername = "newuser";
        String newEmail = "new@example.com";
        user.setUsername(newUsername);
        user.setEmail(newEmail);

        // THEN : Vérification que les attributs ont été correctement modifiés
        assertEquals(newUsername, user.getUsername(), "Le nom d'utilisateur devrait avoir été modifié.");
        assertEquals(newEmail, user.getEmail(), "L'e-mail devrait avoir été modifié.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void user_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de User
        User user = new User();

        // THEN : Vérification que l'ID est null
        assertNull(user.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }

    // Test pour vérifier la relation OneToMany avec l'entité UserRole
    @Test
    void user_oneToManyRelationWithUserRoles() {
        // GIVEN : Création d'une instance de User et de deux instances de UserRole
        // mockées
        User user = new User();
        UserRole userRole1 = Mockito.mock(UserRole.class);
        UserRole userRole2 = Mockito.mock(UserRole.class);
        List<UserRole> userRoles = Arrays.asList(userRole1, userRole2);

        // WHEN : Association des rôles utilisateur à l'utilisateur
        user.setUserRoles(userRoles);

        // THEN : Vérification que la liste des rôles utilisateur a été correctement
        // définie
        assertEquals(2, user.getUserRoles().size(), "La liste des rôles utilisateur doit contenir deux éléments.");
        assertEquals(userRole1, user.getUserRoles().get(0), "Le premier rôle utilisateur doit correspondre.");
        assertEquals(userRole2, user.getUserRoles().get(1), "Le deuxième rôle utilisateur doit correspondre.");
    }

    // Test pour vérifier que la liste des rôles utilisateur est initialement vide
    // si elle n'est pas définie
    @Test
    void user_userRolesShouldBeEmptyByDefault() {
        // GIVEN : Création d'une instance de User
        User user = new User();

        // THEN : Vérification que la liste des rôles utilisateur est initialement vide
        assertEquals(0, user.getUserRoles().size(), "La liste des rôles utilisateur devrait être vide par défaut.");
    }

    // Test pour vérifier que les champs optionnels (pushToken, stripeCustomerId)
    // peuvent être null
    @Test
    void user_optionalFieldsCanBeNull() {
        // GIVEN : Création d'une instance de User
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstname("Test");
        user.setLastname("User");

        // WHEN : Les champs optionnels ne sont pas définis

        // THEN : Vérification qu'ils sont null par défaut
        assertNull(user.getPushToken(), "Le push token devrait être null par défaut.");
        assertNull(user.getStripeCustomerId(), "L'ID client Stripe devrait être null par défaut.");
    }
}