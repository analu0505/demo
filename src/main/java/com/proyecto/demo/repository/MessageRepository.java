package com.proyecto.demo.repository;

import com.proyecto.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Método para filtrar mensajes por el dueño (ownerId)
    List<Message> findAllByOwnerId(Long ownerId);
}
