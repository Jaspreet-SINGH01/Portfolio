package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PermissionTest {

    // Test pour vérifier la création et la récupération du nom d'une permission
    @Test
    void permission_shouldSetAndGetName() {
        // GIVEN : Création d'un nom de permission
        String permissionName = "READ_VIDEOS";

        // WHEN : Création d'une instance de Permission et définition de son nom
        Permission permission = new Permission();
        permission.setName(permissionName);

        // THEN : Vérification que le nom a été correctement défini et peut être
        // récupéré
        assertNull(permission.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(permissionName, permission.getName(),
                "Le nom de la permission doit correspondre à la valeur définie.");
    }

    // Test pour vérifier que le nom d'une permission peut être modifié
    @Test
    void permission_shouldAllowNameModification() {
        // GIVEN : Création d'une instance de Permission et définition d'un nom initial
        Permission permission = new Permission();
        permission.setName("WRITE_COMMENTS");

        // WHEN : Modification du nom de la permission
        String newPermissionName = "DELETE_VIDEOS";
        permission.setName(newPermissionName);

        // THEN : Vérification que le nom a été correctement modifié
        assertEquals(newPermissionName, permission.getName(), "Le nom de la permission devrait avoir été modifié.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void permission_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de Permission
        Permission permission = new Permission();

        // THEN : Vérification que l'ID est null
        assertNull(permission.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }
}