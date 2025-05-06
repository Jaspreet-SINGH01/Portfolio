package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class PermissionRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private PermissionRepository permissionRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité Permission pour les
    // tests.
    private Permission createAndPersistPermission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        return entityManager.persistAndFlush(permission);
    }

    // Test pour vérifier la méthode findByName du repository lorsque la permission
    // existe.
    @Test
    void findByName_shouldReturnPermissionForGivenName() {
        // GIVEN : Création et persistence d'une permission avec un nom spécifique.
        String permissionName = "VIEW_MOVIES";
        Permission persistedPermission = createAndPersistPermission(permissionName);

        // WHEN : Recherche de la permission par son nom.
        Permission foundPermission = permissionRepository.findByName(permissionName);

        // THEN : Vérification que la permission trouvée n'est pas nulle et a le nom
        // attendu.
        assertEquals(persistedPermission.getId(), foundPermission.getId());
        assertEquals(permissionName, foundPermission.getName());
    }

    // Test pour vérifier la méthode findByName du repository lorsque la permission
    // n'existe pas.
    @Test
    void findByName_shouldReturnNullIfPermissionNotFound() {
        // GIVEN : Aucune permission avec le nom recherché n'est persistée.
        String nonExistentPermissionName = "EDIT_USERS";

        // WHEN : Recherche d'une permission par un nom qui n'existe pas dans la base de
        // données.
        Permission foundPermission = permissionRepository.findByName(nonExistentPermissionName);

        // THEN : Vérification que la méthode retourne null.
        assertNull(foundPermission);
    }
}