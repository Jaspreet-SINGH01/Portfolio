package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import com.videoflix.subscriptions_microservice.repositories.PermissionRepository;
import com.videoflix.subscriptions_microservice.repositories.RoleRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    // @Mock crée un mock du PermissionRepository.
    @Mock
    private PermissionRepository permissionRepository;

    // @Mock crée un mock du RoleRepository.
    @Mock
    private RoleRepository roleRepository;

    // @Mock crée un mock du UserRoleRepository.
    @Mock
    private UserRoleRepository userRoleRepository;

    // @Mock crée un mock du Logger.
    @Mock
    private Logger logger;

    // @InjectMocks crée une instance de RoleService et injecte les mocks annotés
    // avec @Mock.
    @InjectMocks
    private RoleService roleService;

    // Test pour vérifier l'assignation d'un rôle à un utilisateur.
    @Test
    void assignRoleToUser_shouldAssignRoleIfRoleExists() {
        // GIVEN : Un ID utilisateur et un nom de rôle existant.
        Long userId = 1L;
        String roleName = "Editor";
        Role editorRole = new Role();
        editorRole.setId(2L);
        editorRole.setName(roleName);

        // Configuration du mock roleRepository pour retourner le rôle lors de la
        // recherche par nom.
        when(roleRepository.findByName(roleName)).thenReturn(editorRole);

        // WHEN : Appel de la méthode assignRoleToUser.
        roleService.assignRoleToUser(userId, roleName);

        // THEN : Vérification que userRoleRepository.save a été appelé avec un UserRole
        // correct.
        UserRole expectedUserRole = new UserRole();
        expectedUserRole.setId(userId);
        expectedUserRole.setRoleId(editorRole.getId());
        verify(userRoleRepository, times(1)).save(expectedUserRole);
        // Vérification qu'aucun avertissement n'a été logué.
        verify(logger, never()).warn(anyString());
    }

    // Test pour vérifier que l'assignation de rôle n'a pas lieu si le rôle n'existe
    // pas.
    @Test
    void assignRoleToUser_shouldNotAssignRoleIfRoleDoesNotExistAndLogWarning() {
        // GIVEN : Un ID utilisateur et un nom de rôle inexistant.
        Long userId = 1L;
        String nonExistentRoleName = "NonExistentRole";

        // Configuration du mock roleRepository pour retourner null (rôle non trouvé).
        when(roleRepository.findByName(nonExistentRoleName)).thenReturn(null);

        // WHEN : Appel de la méthode assignRoleToUser.
        roleService.assignRoleToUser(userId, nonExistentRoleName);

        // THEN : Vérification que userRoleRepository.save n'a pas été appelé.
        verify(userRoleRepository, never()).save(any());
        // Vérification qu'un avertissement a été logué concernant le rôle non trouvé.
        verify(logger, times(1)).warn("Role with name '{}' not found.", nonExistentRoleName);
    }

    // Test pour vérifier l'assignation de rôle par niveau d'abonnement pour le
    // niveau "Premium".
    @Test
    void assignRoleBySubscriptionLevel_shouldAssignPremiumRoleForPremiumLevel() {
        // GIVEN : Un ID utilisateur et le niveau d'abonnement "Premium".
        Long userId = 3L;
        String subscriptionLevel = "Premium";
        Role premiumRole = new Role();
        premiumRole.setId(4L);
        premiumRole.setName("Abonné Premium");

        // Configuration du mock roleRepository pour retourner le rôle "Abonné Premium".
        when(roleRepository.findByName("Abonné Premium")).thenReturn(premiumRole);

        // WHEN : Appel de la méthode assignRoleBySubscriptionLevel.
        roleService.assignRoleBySubscriptionLevel(userId, subscriptionLevel);

        // THEN : Vérification que userRoleRepository.save a été appelé avec le UserRole
        // correct.
        UserRole expectedUserRole = new UserRole();
        expectedUserRole.setId(userId);
        expectedUserRole.setRoleId(premiumRole.getId());
        verify(userRoleRepository, times(1)).save(expectedUserRole);
        verify(logger, never()).warn(anyString());
    }

    // Test pour vérifier l'assignation de rôle par niveau d'abonnement pour un
    // niveau inconnu.
    @Test
    void assignRoleBySubscriptionLevel_shouldLogWarningForUnknownSubscriptionLevel() {
        // GIVEN : Un ID utilisateur et un niveau d'abonnement inconnu.
        Long userId = 5L;
        String unknownLevel = "Diamond";

        // WHEN : Appel de la méthode assignRoleBySubscriptionLevel.
        roleService.assignRoleBySubscriptionLevel(userId, unknownLevel);

        // THEN : Vérification que userRoleRepository.save n'a pas été appelé.
        verify(userRoleRepository, never()).save(any());
        // Vérification qu'un avertissement a été logué concernant le niveau inconnu.
        verify(logger, times(1)).warn("Unknown subscription level: '{}'", unknownLevel);
    }

    // Test pour vérifier la création d'une permission.
    @Test
    void createPermission_shouldCreateAndReturnNewPermission() {
        // GIVEN : Un nom de permission.
        String permissionName = "create_video";
        Permission newPermission = new Permission();
        newPermission.setName(permissionName);
        newPermission.setId(6L);

        // Configuration du mock permissionRepository pour retourner la permission
        // sauvegardée.
        when(permissionRepository.save(any(Permission.class))).thenReturn(newPermission);

        // WHEN : Appel de la méthode createPermission.
        Permission createdPermission = roleService.createPermission(permissionName);

        // THEN : Vérification que permissionRepository.save a été appelé et que la
        // permission retournée est correcte.
        verify(permissionRepository, times(1)).save(argThat(p -> p.getName().equals(permissionName)));
        assertEquals(newPermission, createdPermission);
    }

    // Test pour vérifier la suppression d'une permission existante.
    @Test
    void deletePermission_shouldDeletePermissionIfFound() {
        // GIVEN : Un nom de permission existant.
        String permissionToDelete = "delete_video";
        Permission existingPermission = new Permission();
        existingPermission.setName(permissionToDelete);
        existingPermission.setId(7L);

        // Configuration du mock permissionRepository pour retourner la permission lors
        // de la recherche.
        when(permissionRepository.findByName(permissionToDelete)).thenReturn(existingPermission);

        // WHEN : Appel de la méthode deletePermission.
        roleService.deletePermission(permissionToDelete);

        // THEN : Vérification que permissionRepository.delete a été appelé avec la
        // permission correcte.
        verify(permissionRepository, times(1)).delete(existingPermission);
    }

    // Test pour vérifier que la suppression n'a pas lieu si la permission n'existe
    // pas.
    @Test
    void deletePermission_shouldNotDeletePermissionIfNotFound() {
        // GIVEN : Un nom de permission inexistant.
        String nonExistentPermission = "non_existent_permission";

        // Configuration du mock permissionRepository pour retourner null.
        when(permissionRepository.findByName(nonExistentPermission)).thenReturn(null);

        // WHEN : Appel de la méthode deletePermission.
        roleService.deletePermission(nonExistentPermission);

        // THEN : Vérification que permissionRepository.delete n'a pas été appelé.
        verify(permissionRepository, never()).delete(any());
    }

    // Test pour récupérer les permissions d'un rôle existant.
    @Test
    void getPermissionsForRole_shouldReturnPermissionsIfRoleExists() {
        // GIVEN : Un nom de rôle existant avec des permissions associées.
        String roleWithPermissions = "Moderator";
        Permission permission1 = new Permission();
        permission1.setName("edit_comments");
        Permission permission2 = new Permission();
        permission2.setName("delete_comments");
        Role moderatorRole = new Role();
        moderatorRole.setName(roleWithPermissions);
        moderatorRole.setPermissions(Arrays.asList(permission1, permission2));

        // Configuration du mock roleRepository pour retourner le rôle.
        when(roleRepository.findByName(roleWithPermissions)).thenReturn(moderatorRole);

        // WHEN : Appel de la méthode getPermissionsForRole.
        List<Permission> permissions = roleService.getPermissionsForRole(roleWithPermissions);

        // THEN : Vérification que la liste de permissions retournée est correcte.
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(permission1));
        assertTrue(permissions.contains(permission2));
    }

    // Test pour récupérer une liste vide si le rôle n'existe pas.
    @Test
    void getPermissionsForRole_shouldReturnEmptyListIfRoleDoesNotExist() {
        // GIVEN : Un nom de rôle inexistant.
        String nonExistentRole = "Guest";

        // Configuration du mock roleRepository pour retourner null.
        when(roleRepository.findByName(nonExistentRole)).thenReturn(null);

        // WHEN : Appel de la méthode getPermissionsForRole.
        List<Permission> permissions = roleService.getPermissionsForRole(nonExistentRole);

        // THEN : Vérification que la liste retournée est vide.
        assertTrue(permissions.isEmpty());
    }

    // Test pour créer un rôle avec des permissions.
    @Test
    void createRole_shouldCreateRoleWithGivenPermissions() {
        // GIVEN : Un nom de rôle et une liste de noms de permissions existantes.
        String newRoleName = "ContentManager";
        List<String> permissionNames = Arrays.asList("upload_video", "edit_video");
        Permission permission1 = new Permission();
        permission1.setName("upload_video");
        permission1.setId(8L);
        Permission permission2 = new Permission();
        permission2.setName("edit_video");
        permission2.setId(9L);
        Role savedRole = new Role();
        savedRole.setName(newRoleName);
        savedRole.setPermissions(Arrays.asList(permission1, permission2));
        savedRole.setId(10L);

        // Configuration des mocks permissionRepository et roleRepository.
        when(permissionRepository.findByName("upload_video")).thenReturn(permission1);
        when(permissionRepository.findByName("edit_video")).thenReturn(permission2);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // WHEN : Appel de la méthode createRole.
        Role createdRole = roleService.createRole(newRoleName, permissionNames);

        // THEN : Vérification que roleRepository.save a été appelé avec le rôle correct
        // et que le rôle retourné est correct.
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals(newRoleName) &&
                role.getPermissions().size() == 2 &&
                role.getPermissions().contains(permission1) &&
                role.getPermissions().contains(permission2)));
        assertEquals(savedRole, createdRole);
    }

    // Test pour supprimer un rôle existant.
    @Test
    void deleteRole_shouldDeleteRoleIfFound() {
        // GIVEN : Un nom de rôle existant.
        String roleToDelete = "Viewer";
        Role existingRole = new Role();
        existingRole.setName(roleToDelete);
        existingRole.setId(11L);

        // Configuration du mock roleRepository.
        when(roleRepository.findByName(roleToDelete)).thenReturn(existingRole);

        // WHEN : Appel de la méthode deleteRole.
        roleService.deleteRole(roleToDelete);

        // THEN : Vérification que roleRepository.delete a été appelé.
        verify(roleRepository, times(1)).delete(existingRole);
    }

    // Test pour ne pas supprimer un rôle inexistant.
    @Test
    void deleteRole_shouldNotDeleteRoleIfNotFound() {
        // GIVEN : Un nom de rôle inexistant.
        String nonExistentRole = "SuperUser";

        // Configuration du mock roleRepository.
        when(roleRepository.findByName(nonExistentRole)).thenReturn(null);

        // WHEN : Appel de la méthode deleteRole.
        roleService.deleteRole(nonExistentRole);

        // THEN : Vérification que roleRepository.delete n'a pas été appelé.
        verify(roleRepository, never()).delete(any());
    }

    // Test pour ajouter des permissions à un rôle existant.
    @Test
    void addPermissionsToRole_shouldAddPermissionsToExistingRole() {
        // GIVEN : Un nom de rôle existant et une liste de noms de permissions
        // existantes.
        String roleToUpdate = "Editor";
        List<String> permissionsToAdd = Arrays.asList("publish_article");
        Role editorRole = new Role();
        editorRole.setName(roleToUpdate);
        editorRole.setId(12L);
        editorRole.setPermissions(new ArrayList<>());
        Permission permissionToAdd = new Permission();
        permissionToAdd.setName("publish_article");
        permissionToAdd.setId(13L);

        // Configuration des mocks.
        when(roleRepository.findByName(roleToUpdate)).thenReturn(editorRole);
        when(permissionRepository.findByName("publish_article")).thenReturn(permissionToAdd);
        when(roleRepository.save(editorRole)).thenReturn(editorRole);

        // WHEN : Appel de la méthode addPermissionsToRole.
        roleService.addPermissionsToRole(roleToUpdate, permissionsToAdd);

        // THEN : Vérification que la permission a été ajoutée et que
        // roleRepository.save a été appelé.
        assertEquals(1, editorRole.getPermissions().size());
        assertTrue(editorRole.getPermissions().contains(permissionToAdd));
        verify(roleRepository, times(1)).save(editorRole);
    }

    // Test pour supprimer des permissions d'un rôle existant.
    @Test
    void removePermissionsFromRole_shouldRemovePermissionsFromExistingRole() {
        // GIVEN : Un nom de rôle existant et une liste de noms de permissions à
        // supprimer.
        String roleToUpdate = "Moderator";
        List<String> permissionsToRemove = Arrays.asList("delete_comments");
        Permission permissionToRemove = new Permission();
        permissionToRemove.setName("delete_comments");
        permissionToRemove.setId(14L);
        Role moderatorRole = new Role();
        moderatorRole.setName(roleToUpdate);
        moderatorRole.setId(15L);
        moderatorRole.setPermissions(new ArrayList<>(Arrays.asList(new Permission(), permissionToRemove)));

        // Configuration des mocks.
        when(roleRepository.findByName(roleToUpdate)).thenReturn(moderatorRole);
        when(permissionRepository.findByName("delete_comments")).thenReturn(permissionToRemove);
        when(roleRepository.save(moderatorRole)).thenReturn(moderatorRole);

        // WHEN : Appel de la méthode removePermissionsFromRole.
        roleService.removePermissionsFromRole(roleToUpdate, permissionsToRemove);

        // THEN : Vérification que la permission a été supprimée et que
        // roleRepository.save a été appelé.
        assertEquals(1, moderatorRole.getPermissions().size());
        assertFalse(moderatorRole.getPermissions().contains(permissionToRemove));
        verify(roleRepository, times(1)).save(moderatorRole);
    }

    // Test pour récupérer tous les rôles.
    @Test
    void getAllRoles_shouldReturnAllRolesFromRepository() {
        // GIVEN : Une liste de rôles dans le repository.
        Role role1 = new Role();
        role1.setName("Admin");
        Role role2 = new Role();
        role2.setName("User");
        List<Role> allRoles = Arrays.asList(role1, role2);

        // Configuration du mock roleRepository.
        when(roleRepository.findAll()).thenReturn(allRoles);

        // WHEN : Appel de la méthode getAllRoles.
        List<Role> result = roleService.getAllRoles();

        // THEN : Vérification que la liste retournée correspond à celle du repository.
        assertEquals(allRoles, result);
    }

    // Test pour récupérer les rôles d'un utilisateur.
    @Test
    void getRolesForUser_shouldReturnRolesForGivenUserId() {
        // GIVEN : Un ID utilisateur et des UserRole liant cet utilisateur à des rôles.
        Long userId = 16L;
        UserRole userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setRoleId(17L);
        UserRole userRole2 = new UserRole();
        userRole2.setId(1L);
        userRole2.setRoleId(18L);
        Role role1 = new Role();
        role1.setId(17L);
        role1.setName("Content Creator");
        Role role2 = new Role();
        role2.setId(18L);
        role2.setName("Viewer");

        // Configuration des mocks.
        when(userRoleRepository.findByUserId(userId)).thenReturn(Arrays.asList(userRole1, userRole2));
        when(roleRepository.findById(17L)).thenReturn(Optional.of(role1));
        when(roleRepository.findById(18L)).thenReturn(Optional.of(role2));

        // WHEN : Appel de la méthode getRolesForUser.
        List<Role> roles = roleService.getRolesForUser(userId);

        // THEN : Vérification que la liste des rôles retournée est correcte.
        assertEquals(2, roles.size());
        assertTrue(roles.contains(role1));
        assertTrue(roles.contains(role2));
    }

    // Test pour gérer le cas où un rôle associé à un utilisateur n'est pas trouvé.
    @Test
    void getRolesForUser_shouldHandleRoleNotFound() {
        // GIVEN : Un ID utilisateur et un UserRole dont le rôle n'existe pas.
        Long userId = 19L;
        UserRole userRole = new UserRole();
        userRole.setId(1L);
        userRole.setRoleId(20L);

        // Configuration des mocks pour retourner un UserRole mais pas de Role
        // correspondant.
        when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(userRole));
        when(roleRepository.findById(20L)).thenReturn(Optional.empty());

        // WHEN : Appel de la méthode getRolesForUser.
        List<Role> roles = roleService.getRolesForUser(userId);

        // THEN : Vérification que la liste des rôles retournée ne contient pas de
        // valeur nulle (le rôle non trouvé est ignoré).
        assertEquals(0, roles.size());
    }
}