package com.proyecto.demo.repository;

import com.proyecto.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByUsuarioId(Long usuarioId);
}
