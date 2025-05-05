package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.services.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @InjectMocks
    private RoleController roleController; // L'instance du contrôleur à tester

    @Mock
    private RoleService roleService; // Mock du service des rôles

    @Test
    void getAllRoles_shouldReturnOkWithListOfRoles() {
        // GIVEN : Une liste de rôles simulée retournée par le service
        List<Role> roles = Arrays.asList(new Role(), new Role());
        when(roleService.getAllRoles()).thenReturn(roles);

        // WHEN : L'appel à la méthode getAllRoles du contrôleur
        ResponseEntity<List<Role>> response = roleController.getAllRoles();

        // THEN : Vérification que la réponse a le statut OK et contient la liste des
        // rôles
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roles, response.getBody());
        verify(roleService, times(1)).getAllRoles(); // Vérifie que la méthode getAllRoles du service a été appelée une
                                                     // fois
    }

    @Test
    void getPermissionsForRole_shouldReturnOkWithListOfPermissions_whenRoleExists() {
        // GIVEN : Un nom de rôle existant et une liste de permissions correspondante
        // retournée par le service
        String roleName = "ADMIN";
        List<Permission> permissions = Arrays.asList(new Permission(), new Permission());
        when(roleService.getPermissionsForRole(roleName)).thenReturn(permissions);

        // WHEN : L'appel à la méthode getPermissionsForRole du contrôleur
        ResponseEntity<List<Permission>> response = roleController.getPermissionsForRole(roleName);

        // THEN : Vérification que la réponse a le statut OK et contient la liste des
        // permissions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(permissions, response.getBody());
        verify(roleService, times(1)).getPermissionsForRole(roleName); // Vérifie que la méthode getPermissionsForRole
                                                                       // du service a été appelée avec le nom de rôle
                                                                       // correct
    }

    @Test
    void getPermissionsForRole_shouldReturnNotFound_whenRoleDoesNotExist() {
        // GIVEN : Un nom de rôle inexistant (le service retourne null)
        String roleName = "NON_EXISTANT";
        when(roleService.getPermissionsForRole(roleName)).thenReturn(null);

        // WHEN : L'appel à la méthode getPermissionsForRole du contrôleur
        ResponseEntity<List<Permission>> response = roleController.getPermissionsForRole(roleName);

        // THEN : Vérification que la réponse a le statut NotFound
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Le corps de la réponse devrait être null pour un NotFound
        verify(roleService, times(1)).getPermissionsForRole(roleName); // Vérifie que la méthode getPermissionsForRole
                                                                       // du service a été appelée avec le nom de rôle
                                                                       // correct
    }

    @Test
    void getRolesForUser_shouldReturnOkWithListOfRolesForUser() {
        // GIVEN : Un ID utilisateur et une liste de rôles correspondante retournée par
        // le service
        Long userId = 1L;
        List<Role> roles = Arrays.asList(new Role(), new Role());
        when(roleService.getRolesForUser(userId)).thenReturn(roles);

        // WHEN : L'appel à la méthode getRolesForUser du contrôleur
        ResponseEntity<List<Role>> response = roleController.getRolesForUser(userId);

        // THEN : Vérification que la réponse a le statut OK et contient la liste des
        // rôles de l'utilisateur
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roles, response.getBody());
        verify(roleService, times(1)).getRolesForUser(userId); // Vérifie que la méthode getRolesForUser du service a
                                                               // été appelée avec l'ID utilisateur correct
    }

    @Test
    void assignRoleToUser_shouldReturnCreatedWithMessage_whenAssignmentSuccessful() {
        // GIVEN : Un ID utilisateur et un nom de rôle valides
        Long userId = 1L;
        String roleName = "CUSTOMER";
        doNothing().when(roleService).assignRoleToUser(userId, roleName); // Configure le mock pour ne rien faire

        // WHEN : L'appel à la méthode assignRoleToUser du contrôleur
        ResponseEntity<String> response = roleController.assignRoleToUser(userId, roleName);

        // THEN : Vérification que la réponse a le statut Created et contient le message
        // attendu
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Role assigned to user.", response.getBody());
        verify(roleService, times(1)).assignRoleToUser(userId, roleName); // Vérifie que la méthode assignRoleToUser du
                                                                          // service a été appelée avec les bons
                                                                          // paramètres
    }

    @Test
    void assignRoleToUser_shouldReturnNotFound_whenServiceThrowsException() {
        // GIVEN : Un ID utilisateur et un nom de rôle valides, mais le service lève une
        // exception
        Long userId = 1L;
        String roleName = "NON_EXISTANT_ROLE";
        String errorMessage = "User or role not found.";
        doThrow(new IllegalArgumentException(errorMessage)).when(roleService).assignRoleToUser(userId, roleName);

        // WHEN : L'appel à la méthode assignRoleToUser du contrôleur
        ResponseEntity<String> response = roleController.assignRoleToUser(userId, roleName);

        // THEN : Vérification que la réponse a le statut NotFound et potentiellement un
        // message d'erreur
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody()); // Vérifie que le message de l'exception est renvoyé
        verify(roleService, times(1)).assignRoleToUser(userId, roleName); // Vérifie que la méthode du service a été
                                                                          // appelée
    }
}