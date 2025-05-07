package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class UserRoleRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private UserRoleRepository userRoleRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité User pour les tests.
    private User createAndPersistUser(Long id) {
        User user = new User();
        user.setId(id);
        return entityManager.persistAndFlush(user);
    }

    // Méthode utilitaire pour créer et persister une entité Role pour les tests.
    private Role createAndPersistRole(String name) {
        Role role = new Role();
        role.setName(name);
        return entityManager.persistAndFlush(role);
    }

    // Méthode utilitaire pour créer et persister une entité UserRole pour les
    // tests.
    private UserRole createAndPersistUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        return entityManager.persistAndFlush(userRole);
    }

    // Test pour vérifier la méthode findByUserId.
    @Test
    void findByUserId_shouldReturnUserRolesForGivenUserId() {
        // GIVEN : Création et persistence d'un utilisateur et de plusieurs UserRoles
        // associés.
        User user = createAndPersistUser(1L);
        Role role1 = createAndPersistRole("ROLE_USER");
        Role role2 = createAndPersistRole("ROLE_ADMIN");
        createAndPersistUserRole(user, role1);
        createAndPersistUserRole(user, role2);

        User anotherUser = createAndPersistUser(2L);
        Role role3 = createAndPersistRole("ROLE_EDITOR");
        createAndPersistUserRole(anotherUser, role3);

        // WHEN : Recherche des UserRoles par l'ID de l'utilisateur.
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());

        // THEN : Vérification que la liste contient les deux UserRoles associés à
        // l'utilisateur.
        assertEquals(2, userRoles.size());
        assertTrue(userRoles.stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_USER")));
        assertTrue(userRoles.stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_ADMIN")));
    }

    // Test pour vérifier la méthode findByUserId lorsqu'aucun UserRole n'est trouvé
    // pour l'ID utilisateur donné.
    @Test
    void findByUserId_shouldReturnEmptyListIfNoUserRolesForUserId() {
        // GIVEN : Création d'un utilisateur sans UserRoles associés.
        User user = createAndPersistUser(3L);

        // WHEN : Recherche des UserRoles par l'ID de cet utilisateur.
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());

        // THEN : Vérification que la liste est vide.
        assertTrue(userRoles.isEmpty());
    }

    // Test pour vérifier la méthode findByUser.
    @Test
    void findByUser_shouldReturnUserRolesForGivenUserEntity() {
        // GIVEN : Création et persistence d'un utilisateur et de plusieurs UserRoles
        // associés.
        User user = createAndPersistUser(4L);
        Role role1 = createAndPersistRole("ROLE_VIEWER");
        Role role2 = createAndPersistRole("ROLE_SUBSCRIBER");
        createAndPersistUserRole(user, role1);
        createAndPersistUserRole(user, role2);

        User anotherUser = createAndPersistUser(5L);
        Role role3 = createAndPersistRole("ROLE_GUEST");
        createAndPersistUserRole(anotherUser, role3);

        // WHEN : Recherche des UserRoles par l'entité utilisateur.
        List<UserRole> userRoles = userRoleRepository.findByUser(user);

        // THEN : Vérification que la liste contient les deux UserRoles associés à
        // l'utilisateur.
        assertEquals(2, userRoles.size());
        assertTrue(userRoles.stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_VIEWER")));
        assertTrue(userRoles.stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_SUBSCRIBER")));
        assertTrue(userRoles.stream().allMatch(ur -> ur.getUser().getId().equals(user.getId())));
    }

    // Test pour vérifier la méthode findByUser lorsqu'aucun UserRole n'est trouvé
    // pour l'entité utilisateur donnée.
    @Test
    void findByUser_shouldReturnEmptyListIfNoUserRolesForUserEntity() {
        // GIVEN : Création d'un utilisateur sans UserRoles associés.
        User user = createAndPersistUser(6L);

        // WHEN : Recherche des UserRoles par cette entité utilisateur.
        List<UserRole> userRoles = userRoleRepository.findByUser(user);

        // THEN : Vérification que la liste est vide.
        assertTrue(userRoles.isEmpty());
    }
}