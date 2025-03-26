package com.videoflix.Users.entities;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.entities.Permission;
import com.videoflix.users_microservice.entities.Role;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void roleEnum_ShouldHaveCorrectPermissionsForUser() {
        // Teste que le rôle USER a les permissions attendues

        // Récupération des permissions du rôle USER
        Set<Permission> userPermissions = Role.USER.getPermissions();

        // Vérification que les permissions attendues sont présentes
        assertEquals(3, userPermissions.size());
        assertTrue(userPermissions.contains(Permission.VIDEO_VIEW));
        assertTrue(userPermissions.contains(Permission.VIDEO_LIKE_DISLIKE));
        assertTrue(userPermissions.contains(Permission.PLAYLIST_ADD_VIDEO));
    }

    @Test
    void roleEnum_ShouldHaveCorrectPermissionsForAdmin() {
        // Teste que le rôle ADMIN a les permissions attendues

        // Récupération des permissions du rôle ADMIN
        Set<Permission> adminPermissions = Role.ADMIN.getPermissions();

        // Vérification que les permissions attendues sont présentes
        assertEquals(7, adminPermissions.size());
        assertTrue(adminPermissions.contains(Permission.VIDEO_VIEW));
        assertTrue(adminPermissions.contains(Permission.VIDEO_LIKE_DISLIKE));
        assertTrue(adminPermissions.contains(Permission.PLAYLIST_ADD_VIDEO));
        assertTrue(adminPermissions.contains(Permission.VIDEO_ADD));
        assertTrue(adminPermissions.contains(Permission.VIDEO_UPDATE));
        assertTrue(adminPermissions.contains(Permission.VIDEO_DELETE));
        assertTrue(adminPermissions.contains(Permission.VIDEO_ADD_DESCRIPTION));
    }

    @Test
    void roleEnum_ShouldHaveCorrectNumberOfRoles() {
        // Teste que l'énumération Role a le nombre de rôles attendu

        // Vérification du nombre de rôles
        assertEquals(2, Role.values().length);
    }

    @Test
    void roleEnum_ShouldHaveCorrectOrdinalValues() {
        // Teste que les valeurs ordinales de l'énumération sont correctes

        // Vérification des valeurs ordinales (index) de chaque rôle
        assertEquals(0, Role.USER.ordinal());
        assertEquals(1, Role.ADMIN.ordinal());
    }

    @Test
    void roleEnum_ShouldReturnCorrectValuesFromString() {
        // Teste que la méthode valueOf() renvoie les valeurs correctes à partir de
        // chaînes de caractères

        // Vérification que valueOf() renvoie les rôles corrects pour chaque nom de rôle
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }
}