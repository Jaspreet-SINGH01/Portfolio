package com.videoflix.users_microservice.controller;

import com.videoflix.users_microservice.dto.UserDTO;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
public class OAuth2Controller {

    private final UserRepository userRepository;

    /**
     * Constructeur pour OAuth2Controller.
     * Injecte le UserRepository pour accéder à la base de données des utilisateurs.
     *
     * @param userRepository Le repository pour les utilisateurs.
     */
    public OAuth2Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Endpoint pour gérer le succès de l'authentification OAuth2.
     * Récupère les informations de l'utilisateur à partir de l'objet Principal,
     * vérifie si l'utilisateur existe dans la base de données, et le crée ou le met
     * à jour.
     *
     * @param principal Les informations de l'utilisateur authentifié via OAuth2.
     * @return ResponseEntity contenant un UserDTO en cas de succès, ou une réponse
     *         d'erreur en cas d'échec.
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<UserDTO> oauth2Success(Principal principal) {
        try {
            // Caste le Principal en OAuth2User pour accéder aux attributs de l'utilisateur.
            OAuth2User oauth2User = (OAuth2User) principal;

            // Récupère l'email et le nom de l'utilisateur à partir des attributs OAuth2.
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            // Vérifie si l'email ou le nom est nul.
            if (email == null || name == null) {
                // Si l'email ou le nom est nul, renvoie une réponse BAD_REQUEST.
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Vérifie si l'utilisateur existe déjà dans la base de données par son email.
            Optional<User> existingUser = userRepository.findByEmail(email);

            User user;
            // Si l'utilisateur existe déjà, met à jour son nom.
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setName(name);
                userRepository.save(user);
            } else {
                // Si l'utilisateur n'existe pas, crée un nouvel utilisateur.
                user = new User();
                user.setEmail(email);
                user.setName(name);
                userRepository.save(user);
            }

            // Crée un UserDTO pour renvoyer les informations de l'utilisateur.
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setUsername(user.getName());

            // Renvoie une réponse OK avec le UserDTO.
            return ResponseEntity.ok(userDTO);

        } catch (OAuth2AuthenticationException e) {
            // Gère les exceptions d'authentification OAuth2.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            // Gère les autres exceptions.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}