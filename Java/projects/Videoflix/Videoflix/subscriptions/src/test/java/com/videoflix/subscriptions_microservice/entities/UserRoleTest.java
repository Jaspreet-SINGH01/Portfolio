package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRoleTest {

    // Test pour vérifier la création et la récupération des entités User et Role
    // associées
    @Test
    void userRole_shouldSetAndGetUserAndRole() {
        // GIVEN : Création d'objets mockés pour User et Role
        User mockUser = Mockito.mock(User.class);
        Role mockRole = Mockito.mock(Role.class);

        // WHEN : Création d'une instance de UserRole et association des entités
        UserRole userRole = new UserRole();
        userRole.setUser(mockUser);
        userRole.setRole(mockRole);

        // THEN : Vérification que les entités associées ont été correctement définies
        // et peuvent être récupérées
        assertNull(userRole.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockUser, userRole.getUser(), "L'utilisateur associé doit correspondre.");
        assertEquals(mockRole, userRole.getRole(), "Le rôle associé doit correspondre.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void userRole_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de UserRole
        UserRole userRole = new UserRole();

        // THEN : Vérification que l'ID est null
        assertNull(userRole.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }

    // Test pour vérifier le comportement de la méthode setRoleId (qui lève une
    // exception)
    @Test
    void setRoleId_shouldThrowUnsupportedOperationException() {
        // GIVEN : Création d'une instance de UserRole
        UserRole userRole = new UserRole();
        Long roleId = 1L;

        // WHEN & THEN : Vérification qu'une UnsupportedOperationException est lancée
        // lors de l'appel à setRoleId
        assertThrows(UnsupportedOperationException.class, () -> userRole.setRoleId(roleId),
                "setRoleId ne devrait pas être implémentée et devrait lancer une exception.");
    }
}