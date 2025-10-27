package com.proyecto.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // title
    @Column(nullable = false)
    private String title;

    // content (texto plano)
    @Column(columnDefinition = "TEXT")
    private String content;

    // ownerId (Foreign Key, sin necesidad de @ManyToOne por ahora)
    @Column(nullable = false)
    private Long ownerId; 

    // createdAt
    private LocalDateTime createdAt = LocalDateTime.now();

    // updatedAt
    private LocalDateTime updatedAt;

    // Se ejecuta antes de actualizar la entidad
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}