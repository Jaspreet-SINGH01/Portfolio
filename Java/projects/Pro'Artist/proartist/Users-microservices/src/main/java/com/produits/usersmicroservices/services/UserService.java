package com.produits.usersmicroservices.services;

import com.produits.usersmicroservices.entities.Role;
import com.produits.usersmicroservices.entities.User;

public interface UserService {
    User saveUser(User user);
    User findUserByUsername (String username);
    Role addRole(Role role);
    User addRoleToUser(String username, String rolename);
}

