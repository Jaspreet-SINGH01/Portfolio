package com.videoflix.users_microservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.videoflix.users_microservice.dto.UserDTO;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.services.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User createdUser = userService.createUser(user);
        return convertToDTO(createdUser);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getName());
        userDTO.setEmail(user.getEmail());
        // Ne copiez pas le mot de passe !
        return userDTO;
    }
    
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        return user;
    }
}