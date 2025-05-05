package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.dtos.AdminUpdateRequest;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import com.videoflix.subscriptions_microservice.services.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController; // L'instance du contrôleur à tester

    @Mock
    private UserRepository userRepository; // Mock du repository pour les utilisateurs

    @Mock
    private RoleService roleService; // Mock du service pour les rôles

    @Mock
    private AdminUpdateRequest updateRequest;

    @Test
    void getAllUsers_shouldReturnOkWithListOfUsers() {
        // GIVEN : Une liste d'utilisateurs à retourner par le repository
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        // WHEN : L'appel à la méthode getAllUsers du contrôleur
        ResponseEntity<List<User>> response = adminController.getAllUsers();

        // THEN : Vérification que la réponse est OK et contient la liste des
        // utilisateurs
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userRepository, times(1)).findAll(); // Vérifie que la méthode findAll du repository a été appelée une
                                                    // fois
    }

    @Test
    void getUserById_shouldReturnOkWithUser_whenUserExists() {
        // GIVEN : Un ID utilisateur existant et un utilisateur correspondant retourné
        // par le repository
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN : L'appel à la méthode getUserById du contrôleur
        ResponseEntity<User> response = adminController.getUserById(userId);

        // THEN : Vérification que la réponse est OK et contient l'utilisateur
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userRepository, times(1)).findById(userId); // Vérifie que la méthode findById du repository a été
                                                           // appelée avec l'ID correct
    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() {
        // GIVEN : Un ID utilisateur inexistant (le repository retourne un Optional
        // vide)
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN : L'appel à la méthode getUserById du contrôleur
        ResponseEntity<User> response = adminController.getUserById(userId);

        // THEN : Vérification que la réponse est NotFound
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Le corps de la réponse devrait être null pour un NotFound
        verify(userRepository, times(1)).findById(userId); // Vérifie que la méthode findById du repository a été
                                                           // appelée avec l'ID correct
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenUserExists() {
        // GIVEN : Un ID utilisateur existant (le repository retourne true pour
        // existsById)
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId); // Configure le mock pour ne rien faire lors de la
                                                             // suppression

        // WHEN : L'appel à la méthode deleteUser du contrôleur
        ResponseEntity<Void> response = adminController.deleteUser(userId);

        // THEN : Vérification que la réponse est NoContent et que la méthode deleteById
        // du repository a été appelée
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userRepository, times(1)).existsById(userId); // Vérifie que existsById a été appelé
        verify(userRepository, times(1)).deleteById(userId); // Vérifie que deleteById a été appelé avec l'ID correct
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() {
        // GIVEN : Un ID utilisateur inexistant (le repository retourne false pour
        // existsById)
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // WHEN : L'appel à la méthode deleteUser du contrôleur
        ResponseEntity<Void> response = adminController.deleteUser(userId);

        // THEN : Vérification que la réponse est NotFound et que la méthode deleteById
        // du repository n'a pas été appelée
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userRepository, times(1)).existsById(userId); // Vérifie que existsById a été appelé
        verify(userRepository, never()).deleteById(userId); // Vérifie que deleteById n'a pas été appelé
    }

    @Test
    void getAllRoles_shouldReturnOkWithListOfRoles() {
        // GIVEN : Une liste de rôles à retourner par le service de rôles
        List<Role> roles = Arrays.asList(new Role(), new Role());
        when(roleService.getAllRoles()).thenReturn(roles);

        // WHEN : L'appel à la méthode getAllRoles du contrôleur
        ResponseEntity<List<Role>> response = adminController.getAllRoles();

        // THEN : Vérification que la réponse est OK et contient la liste des rôles
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roles, response.getBody());
        verify(roleService, times(1)).getAllRoles(); // Vérifie que la méthode getAllRoles du service a été appelée une
                                                     // fois
    }

    @Test
    void assignRoleToUser_shouldReturnOkWithMessage_whenAssignmentSuccessful() {
        // GIVEN : Un ID utilisateur et un nom de rôle valides (le service ne lève pas
        // d'exception)
        Long userId = 1L;
        String roleName = "TEST_ROLE";
        doNothing().when(roleService).assignRoleToUser(userId, roleName);

        // WHEN : L'appel à la méthode assignRoleToUser du contrôleur
        ResponseEntity<String> response = adminController.assignRoleToUser(userId, roleName);

        // THEN : Vérification que la réponse est OK avec le message attendu
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Role 'TEST_ROLE' assigned to user 1", response.getBody());
        verify(roleService, times(1)).assignRoleToUser(userId, roleName); // Vérifie que la méthode assignRoleToUser du
                                                                          // service a été appelée
    }

    @Test
    void assignRoleToUser_shouldReturnNotFoundWithMessage_whenAssignmentFails() {
        // GIVEN : Un ID utilisateur et un nom de rôle invalides (le service lève une
        // IllegalArgumentException)
        Long userId = 1L;
        String roleName = "INVALID_ROLE";
        String errorMessage = "Role not found";
        doThrow(new IllegalArgumentException(errorMessage)).when(roleService).assignRoleToUser(userId, roleName);

        // WHEN : L'appel à la méthode assignRoleToUser du contrôleur
        ResponseEntity<String> response = adminController.assignRoleToUser(userId, roleName);

        // THEN : Vérification que la réponse est NotFound avec le message d'erreur
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(roleService, times(1)).assignRoleToUser(userId, roleName); // Vérifie que la méthode assignRoleToUser du
                                                                          // service a été appelée
    }

    @Test
    void getRolesForUser_shouldReturnOkWithListOfRoles_whenUserExists() {
        // GIVEN : Un ID utilisateur existant et une liste de rôles retournée par le
        // service
        Long userId = 1L;
        List<Role> roles = Arrays.asList(new Role(), new Role());
        when(roleService.getRolesForUser(userId)).thenReturn(roles);

        // WHEN : L'appel à la méthode getRolesForUser du contrôleur
        ResponseEntity<List<Role>> response = adminController.getRolesForUser(userId);

        // THEN : Vérification que la réponse est OK et contient la liste des rôles
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roles, response.getBody());
        verify(roleService, times(1)).getRolesForUser(userId); // Vérifie que la méthode getRolesForUser du service a
                                                               // été appelée
    }

    @Test
    void getRolesForUser_shouldReturnNotFoundWithEmptyList_whenUserDoesNotExist() {
        // GIVEN : Un ID utilisateur inexistant (le service lève une
        // IllegalArgumentException)
        Long userId = 1L;
        String errorMessage = "User not found";
        when(roleService.getRolesForUser(userId)).thenThrow(new IllegalArgumentException(errorMessage));

        // WHEN : L'appel à la méthode getRolesForUser du contrôleur
        ResponseEntity<List<Role>> response = adminController.getRolesForUser(userId);

        // THEN : Vérification que la réponse est NotFound et contient une liste vide
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verify(roleService, times(1)).getRolesForUser(userId); // Vérifie que la méthode getRolesForUser du service a
                                                               // été appelée
    }

    @Test
    void updateUser_shouldReturnOkWithMessage_whenValidationPasses() {
        // GIVEN : Un ID utilisateur et un AdminUpdateRequest valide
        Long userId = 1L;
        updateRequest = new AdminUpdateRequest(null, userId); // Assume qu'il est valide pour ce test

        // WHEN : L'appel à la méthode updateUser du contrôleur
        ResponseEntity<String> response = adminController.updateUser(userId, updateRequest);

        // THEN : Vérification que la réponse est OK avec le message attendu
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur mis à jour.", response.getBody());
        // Note : Dans ce test, nous ne vérifions pas l'interaction avec le
        // userRepository
        // car la logique de mise à jour réelle n'est pas dans le contrôleur mais dans
        // un service.
        // Un test d'intégration serait plus approprié pour vérifier la mise à jour
        // effective.
    }
}