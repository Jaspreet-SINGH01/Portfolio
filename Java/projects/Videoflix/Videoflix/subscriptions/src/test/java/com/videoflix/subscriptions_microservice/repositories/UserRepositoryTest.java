package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class UserRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private UserRepository userRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité User pour les tests.
    private User createAndPersistUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
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

    // Test pour vérifier la méthode findByUsername.
    @Test
    void findByUsername_shouldReturnUserForGivenUsername() {
        // GIVEN : Création et persistence d'un utilisateur avec un nom d'utilisateur
        // spécifique.
        String username = "testuser";
        User persistedUser = createAndPersistUser(username, "test@example.com");

        // WHEN : Recherche de l'utilisateur par son nom d'utilisateur.
        User foundUser = userRepository.findByUsername(username);

        // THEN : Vérification que l'utilisateur trouvé n'est pas nul et a le nom
        // d'utilisateur attendu.
        assertEquals(persistedUser.getId(), foundUser.getId());
        assertEquals(username, foundUser.getUsername());
    }

    // Test pour vérifier la méthode findByUsername lorsque l'utilisateur n'existe
    // pas.
    @Test
    void findByUsername_shouldReturnNullIfUserNotFoundByUsername() {
        // GIVEN : Aucun utilisateur avec le nom d'utilisateur recherché n'est persisté.
        String nonExistentUsername = "nonexistentuser";

        // WHEN : Recherche d'un utilisateur par un nom d'utilisateur qui n'existe pas.
        User foundUser = userRepository.findByUsername(nonExistentUsername);

        // THEN : Vérification que la méthode retourne null.
        assertNull(foundUser);
    }

    // Test pour vérifier la méthode findByEmail.
    @Test
    void findByEmail_shouldReturnUserForGivenEmail() {
        // GIVEN : Création et persistence d'un utilisateur avec une adresse e-mail
        // spécifique.
        String email = "test@example.com";
        User persistedUser = createAndPersistUser("testuser", email);

        // WHEN : Recherche de l'utilisateur par son adresse e-mail.
        User foundUser = userRepository.findByEmail(email);

        // THEN : Vérification que l'utilisateur trouvé n'est pas nul et a l'adresse
        // e-mail attendue.
        assertEquals(persistedUser.getId(), foundUser.getId());
        assertEquals(email, foundUser.getEmail());
    }

    // Test pour vérifier la méthode findByEmail lorsque l'utilisateur n'existe pas.
    @Test
    void findByEmail_shouldReturnNullIfUserNotFoundByEmail() {
        // GIVEN : Aucun utilisateur avec l'adresse e-mail recherchée n'est persisté.
        String nonExistentEmail = "nonexistent@example.com";

        // WHEN : Recherche d'un utilisateur par une adresse e-mail qui n'existe pas.
        User foundUser = userRepository.findByEmail(nonExistentEmail);

        // THEN : Vérification que la méthode retourne null.
        assertNull(foundUser);
    }

    // Test pour vérifier la méthode findByIdWithRoles, en s'assurant que les rôles
    // de l'utilisateur sont également récupérés.
    @Test
    @Transactional // Utilisé car la récupération de la relation lazy userRoles se produit dans le
                   // contexte de la transaction.
    void findByIdWithRoles_shouldReturnUserWithFetchedRoles() {
        // GIVEN : Création et persistence d'un utilisateur et de ses rôles.
        User user = createAndPersistUser("userwithroles", "roles@example.com");
        Role role1 = createAndPersistRole("ROLE_USER");
        Role role2 = createAndPersistRole("ROLE_ADMIN");
        createAndPersistUserRole(user, role1);
        createAndPersistUserRole(user, role2);
        entityManager.flush(); // S'assurer que tout est persisté avant la récupération.
        entityManager.clear(); // Détacher l'entité User précédemment gérée pour forcer une nouvelle requête.

        // WHEN : Recherche de l'utilisateur par son ID en récupérant également ses
        // rôles.
        Optional<User> foundUserOptional = userRepository.findByIdWithRoles(user.getId());

        // THEN : Vérification que l'Optional contient l'utilisateur et que ses rôles
        // ont été récupérés.
        assertTrue(foundUserOptional.isPresent());
        User foundUser = foundUserOptional.get();
        assertEquals(user.getId(), foundUser.getId());
        assertNotNull(foundUser.getUserRoles());
        assertEquals(2, foundUser.getUserRoles().size());
        assertTrue(foundUser.getUserRoles().stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_USER")));
        assertTrue(foundUser.getUserRoles().stream().anyMatch(ur -> ur.getRole().getName().equals("ROLE_ADMIN")));
    }

    // Test pour vérifier la méthode findByIdWithRoles lorsqu'aucun utilisateur
    // n'est trouvé pour l'ID donné.
    @Test
    void findByIdWithRoles_shouldReturnEmptyOptionalIfUserNotFoundWithRoles() {
        // GIVEN : Aucun utilisateur avec l'ID spécifié n'est persisté.
        Long nonExistentUserId = 999L;

        // WHEN : Recherche d'un utilisateur par un ID qui n'existe pas, en tentant de
        // récupérer ses rôles.
        Optional<User> foundUserOptional = userRepository.findByIdWithRoles(nonExistentUserId);

        // THEN : Vérification que l'Optional retourné est vide.
        assertTrue(foundUserOptional.isEmpty());
    }
}