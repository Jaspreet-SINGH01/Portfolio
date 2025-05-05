package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class RoleTest {

    // Test pour vérifier la création et la récupération du nom d'un rôle
    @Test
    void role_shouldSetAndGetName() {
        // GIVEN : Création d'un nom de rôle
        String roleName = "ADMIN";

        // WHEN : Création d'une instance de Role et définition de son nom
        Role role = new Role();
        role.setName(roleName);

        // THEN : Vérification que le nom a été correctement défini et peut être
        // récupéré
        assertNull(role.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(roleName, role.getName(), "Le nom du rôle doit correspondre à la valeur définie.");
    }

    // Test pour vérifier que le nom d'un rôle peut être modifié
    @Test
    void role_shouldAllowNameModification() {
        // GIVEN : Création d'une instance de Role et définition d'un nom initial
        Role role = new Role();
        role.setName("BASIC");

        // WHEN : Modification du nom du rôle
        String newRoleName = "PREMIUM";
        role.setName(newRoleName);

        // THEN : Vérification que le nom a été correctement modifié
        assertEquals(newRoleName, role.getName(), "Le nom du rôle devrait avoir été modifié.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void role_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de Role
        Role role = new Role();

        // THEN : Vérification que l'ID est null
        assertNull(role.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }

    // Test pour vérifier l'ajout et la récupération des permissions associées à un
    // rôle
    @Test
    void role_shouldSetAndGetPermissions() {
        // GIVEN : Création de deux instances de Permission mockées
        Permission permission1 = Mockito.mock(Permission.class);
        when(permission1.getName()).thenReturn("READ_VIDEOS");
        Permission permission2 = Mockito.mock(Permission.class);
        when(permission2.getName()).thenReturn("WRITE_COMMENTS");
        List<Permission> permissions = Arrays.asList(permission1, permission2);

        // WHEN : Création d'une instance de Role et association des permissions
        Role role = new Role();
        role.setPermissions(permissions);

        // THEN : Vérification que la liste des permissions a été correctement définie
        // et peut être récupérée
        assertEquals(2, role.getPermissions().size(), "La liste des permissions doit contenir deux éléments.");
        assertEquals("READ_VIDEOS", role.getPermissions().get(0).getName(),
                "La première permission doit être READ_VIDEOS.");
        assertEquals("WRITE_COMMENTS", role.getPermissions().get(1).getName(),
                "La deuxième permission doit être WRITE_COMMENTS.");
    }

    // Test pour vérifier que la liste des permissions peut être modifiée
    @Test
    void role_shouldAllowPermissionsModification() {
        // GIVEN : Création d'une instance de Role et d'une liste de permissions
        // initiale
        Role role = new Role();
        Permission initialPermission = Mockito.mock(Permission.class);
        when(initialPermission.getName()).thenReturn("INITIAL_PERMISSION");
        role.setPermissions(Collections.singletonList(initialPermission));

        // WHEN : Création d'une nouvelle liste de permissions et mise à jour du rôle
        Permission newPermission = Mockito.mock(Permission.class);
        when(newPermission.getName()).thenReturn("NEW_PERMISSION");
        List<Permission> newPermissions = Collections.singletonList(newPermission);
        role.setPermissions(newPermissions);

        // THEN : Vérification que la liste des permissions a été correctement mise à
        // jour
        assertEquals(1, role.getPermissions().size(), "La liste des nouvelles permissions doit contenir un élément.");
        assertEquals("NEW_PERMISSION", role.getPermissions().get(0).getName(),
                "La nouvelle permission doit être NEW_PERMISSION.");
    }

    // Test pour vérifier que la liste des permissions est initialement vide si elle
    // n'est pas définie
    @Test
    void role_permissionsShouldBeEmptyByDefault() {
        // GIVEN : Création d'une instance de Role
        Role role = new Role();

        // THEN : Vérification que la liste des permissions est initialement vide
        assertEquals(0, role.getPermissions().size(), "La liste des permissions devrait être vide par défaut.");
    }
}