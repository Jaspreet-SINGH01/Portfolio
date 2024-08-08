package com.example.users.repositories;

import com.example.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;




public interface UserRepository extends JpaRepository<User, Long> {

    User findByUser_id(Long user_id); // À voir...

    User findByUsername(String username);

    User findByEmail(String email);

    User findByJob(String job);

}

