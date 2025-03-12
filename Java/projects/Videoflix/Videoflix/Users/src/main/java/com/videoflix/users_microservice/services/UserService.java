package com.videoflix.users_microservice.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.InvalidPasswordException;
import com.videoflix.users_microservice.exceptions.UserAlreadyExistsException;
import com.videoflix.users_microservice.exceptions.UserNotFoundException;
import com.videoflix.users_microservice.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        if (!isValidPassword(user.getPassword())) {
            throw new InvalidPasswordException("Mot de passe invalide : doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.");
        }

        if (userRepository.findByName(user.getName()).isPresent()) {
            throw new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé : " + user.getName());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email déjà utilisé : " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
    }

    private boolean isValidPassword(String password) {
        return password != null 
            && password.length() >= 8 
            && password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$");
    }
}