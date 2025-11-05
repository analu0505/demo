package com.proyecto.demo.repository;

import com.proyecto.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Para buscar un usuario por email
    Optional<User> findByEmail(String email);

    // Útil para validaciones rápidas
    boolean existsByEmail(String email);
}
