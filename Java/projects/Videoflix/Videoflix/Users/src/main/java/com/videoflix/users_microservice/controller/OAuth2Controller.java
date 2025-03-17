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

    public OAuth2Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<UserDTO> oauth2Success(Principal principal) {
        try {
            OAuth2User oauth2User = (OAuth2User) principal;

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            if (email == null || name == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Optional<User> existingUser = userRepository.findByEmail(email);

            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setName(name);
                userRepository.save(user);
            } else {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                userRepository.save(user);
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setUsername(user.getName());

            return ResponseEntity.ok(userDTO);

        } catch (OAuth2AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}