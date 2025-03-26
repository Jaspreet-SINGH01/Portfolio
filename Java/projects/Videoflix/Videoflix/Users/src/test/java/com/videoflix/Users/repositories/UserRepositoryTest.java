package com.videoflix.Users.repositories;

import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.repositories.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Indique que c'est un test de couche de données JPA
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository; // Injection du repository à tester

    @Autowired
    private TestEntityManager entityManager; // Injection d'un gestionnaire d'entités de test pour la configuration des
                                             // données

    @Test
    void findByName_ShouldReturnUser_WhenNameExists() {
        // Configuration des données de test
        User user = new User();
        user.setName("TestUser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        entityManager.persist(user); // Persister l'utilisateur dans la base de données de test
        entityManager.flush(); // Garantir que les modifications sont écrites immédiatement

        // Exécution du test
        Optional<User> foundUser = userRepository.findByName("TestUser");

        // Vérification des résultats
        assertTrue(foundUser.isPresent()); // Vérifier que l'utilisateur est trouvé
        assertEquals("TestUser", foundUser.get().getName()); // Vérifier que le nom correspond
    }

    @Test
    void findByName_ShouldReturnEmptyOptional_WhenNameDoesNotExist() {
        // Exécution du test
        Optional<User> foundUser = userRepository.findByName("NonExistentUser");

        // Vérification des résultats
        assertFalse(foundUser.isPresent()); // Vérifier qu'aucun utilisateur n'est trouvé
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        // Configuration des données de test
        User user = new User();
        user.setUsername("testusername");
        user.setEmail("test2@example.com");
        user.setPassword("password2");
        entityManager.persist(user);
        entityManager.flush();

        // Exécution du test
        Optional<User> foundUser = userRepository.findByUsername("testusername");

        // Vérification des résultats
        assertTrue(foundUser.isPresent());
        assertEquals("testusername", foundUser.get().getUsername());
    }

    @Test
    void findByUsername_ShouldReturnEmptyOptional_WhenUsernameDoesNotExist() {
        // Exécution du test
        Optional<User> foundUser = userRepository.findByUsername("nonexistentusername");

        // Vérification des résultats
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Configuration des données de test
        User user = new User();
        user.setEmail("test3@example.com");
        user.setName("testuser3");
        user.setPassword("password3");
        entityManager.persist(user);
        entityManager.flush();

        // Exécution du test
        Optional<User> foundUser = userRepository.findByEmail("test3@example.com");

        // Vérification des résultats
        assertTrue(foundUser.isPresent());
        assertEquals("test3@example.com", foundUser.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmptyOptional_WhenEmailDoesNotExist() {
        // Exécution du test
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Vérification des résultats
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByResetToken_ShouldReturnUser_WhenTokenExists() {
        // Configuration des données de test
        User user = new User();
        user.setEmail("test4@example.com");
        user.setName("testuser4");
        user.setPassword("password4");
        user.setResetToken("testToken");
        entityManager.persist(user);
        entityManager.flush();

        // Exécution du test
        Optional<User> foundUser = userRepository.findByResetToken("testToken");

        // Vérification des résultats
        assertTrue(foundUser.isPresent());
        assertEquals("testToken", foundUser.get().getResetToken());
    }

    @Test
    void findByResetToken_ShouldReturnEmptyOptional_WhenTokenDoesNotExist() {
        // Exécution du test
        Optional<User> foundUser = userRepository.findByResetToken("nonexistentToken");

        // Vérification des résultats
        assertFalse(foundUser.isPresent());
    }

    @Test
    void save_ShouldThrowException_WhenNameIsNull() {
        // Configuration des données de test
        User user = new User();
        user.setEmail("test5@example.com");
        user.setPassword("password5");

        // Exécution et vérification des résultats
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user);
        });
    }

    @Test
    void save_ShouldThrowException_WhenEmailIsNull() {
        // Configuration des données de test
        User user = new User();
        user.setName("testuser5");
        user.setPassword("password5");

        // Exécution et vérification des résultats
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user);
        });
    }

    @Test
    void save_ShouldThrowException_WhenNameIsDuplicate() {
        // Configuration des données de test avec un nom existant
        User existingUser = new User();
        existingUser.setName("DuplicateName");
        existingUser.setEmail("test1@example.com");
        existingUser.setPassword("password1");
        entityManager.persist(existingUser);
        entityManager.flush();

        // Configuration d'un nouvel utilisateur avec le même nom
        User newUser = new User();
        newUser.setName("DuplicateName");
        newUser.setEmail("test2@example.com");
        newUser.setPassword("password2");

        // Exécution et vérification de l'exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(newUser);
        });
    }

    @Test
    void save_ShouldThrowException_WhenEmailIsDuplicate() {
        // Configuration des données de test avec un email existant
        User existingUser = new User();
        existingUser.setName("test1");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("password1");
        entityManager.persist(existingUser);
        entityManager.flush();

        // Configuration d'un nouvel utilisateur avec le même email
        User newUser = new User();
        newUser.setName("test2");
        newUser.setEmail("duplicate@example.com");
        newUser.setPassword("password2");

        // Exécution et vérification de l'exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(newUser);
        });
    }

    @Test
    void save_ShouldThrowException_WhenUsernameIsDuplicate() {
        // Configuration des données de test avec un username existant
        User existingUser = new User();
        existingUser.setUsername("duplicateUsername");
        existingUser.setEmail("test3@example.com");
        existingUser.setPassword("password3");
        entityManager.persist(existingUser);
        entityManager.flush();

        // Configuration d'un nouvel utilisateur avec le même username
        User newUser = new User();
        newUser.setUsername("duplicateUsername");
        newUser.setEmail("test4@example.com");
        newUser.setPassword("password4");

        // Exécution et vérification de l'exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(newUser);
        });
    }
}