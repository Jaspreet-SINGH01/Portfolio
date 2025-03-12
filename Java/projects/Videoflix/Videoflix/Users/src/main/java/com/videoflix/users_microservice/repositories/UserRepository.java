package com.videoflix.users_microservice.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.videoflix.users_microservice.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
}
