package com.proyecto.demo.repository;

import com.proyecto.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // ✅ lo pide UserService.emailExists(...)
    boolean existsByEmail(String email);
}
