package com.proyecto.demo.repository;

import com.proyecto.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Método necesario para verificar la unicidad y para el login
    Optional<User> findByEmail(String email);
}
