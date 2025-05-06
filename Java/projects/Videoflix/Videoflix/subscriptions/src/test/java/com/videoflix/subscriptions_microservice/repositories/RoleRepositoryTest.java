package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class RoleRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private RoleRepository roleRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité Role pour les tests.
    private Role createAndPersistRole(String name) {
        Role role = new Role();
        role.setName(name);
        return entityManager.persistAndFlush(role);
    }

    // Test pour vérifier la méthode findByName du repository lorsque le rôle
    // existe.
    @Test
    void findByName_shouldReturnRoleForGivenName() {
        // GIVEN : Création et persistence d'un rôle avec un nom spécifique.
        String roleName = "ADMIN";
        Role persistedRole = createAndPersistRole(roleName);

        // WHEN : Recherche du rôle par son nom.
        Role foundRole = roleRepository.findByName(roleName);

        // THEN : Vérification que le rôle trouvé n'est pas nul et a le nom attendu.
        assertEquals(persistedRole.getId(), foundRole.getId());
        assertEquals(roleName, foundRole.getName());
    }

    // Test pour vérifier la méthode findByName du repository lorsque le rôle
    // n'existe pas.
    @Test
    void findByName_shouldReturnNullIfRoleNotFound() {
        // GIVEN : Aucun rôle avec le nom recherché n'est persisté.
        String nonExistentRoleName = "CUSTOMER";

        // WHEN : Recherche d'un rôle par un nom qui n'existe pas dans la base de
        // données.
        Role foundRole = roleRepository.findByName(nonExistentRoleName);

        // THEN : Vérification que la méthode retourne null.
        assertNull(foundRole);
    }
}