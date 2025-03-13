package com.videoflix.users_microservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.videoflix.users_microservice.dto.LoginRequest;
import com.videoflix.users_microservice.services.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
    }
}
