package com.proyecto.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.proyecto.demo.model.Role;

@Entity
@Table(name = "users")
@Data // Genera getters, setters, etc. (Requiere dependencia Lombok)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // email (único)
    @Column(unique = true, nullable = false)
    private String email;

    // passwordHash
    @Column(nullable = false)
    private String passwordHash;

    // fullName
    private String fullName;

    // role (ADMIN|USER)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // enabled (para listar/bloquear)
    private boolean enabled = true;

    // createdAt
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Opcional: updatedAt
    private LocalDateTime updatedAt;

    // Se ejecuta antes de actualizar la entidad
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}