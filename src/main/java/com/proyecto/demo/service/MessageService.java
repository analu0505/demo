package com.proyecto.demo.service;

import com.proyecto.demo.model.Message;
import com.proyecto.demo.repository.MessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // CREATE: El owner crea
    public Message create(Message message, Long ownerId) {
        message.setOwnerId(ownerId); // Asegurar que el dueño es el usuario logueado
        return messageRepository.save(message);
    }

    // READ: El owner lee un mensaje específico (verificación de propiedad)
    public Optional<Message> findById(Long id, Long currentUserId) {
        return messageRepository.findById(id)
            .filter(message -> message.getOwnerId().equals(currentUserId));
    }

    // READ: El owner lista sus mensajes
    public List<Message> findAllByOwner(Long ownerId) {
        return messageRepository.findAllByOwnerId(ownerId);
    }

    // UPDATE: El owner actualiza (verificación de propiedad)
    public Message update(Long id, Message updatedDetails, Long currentUserId) {
        return messageRepository.findById(id).map(message -> {
            if (!message.getOwnerId().equals(currentUserId)) {
                throw new SecurityException("Acceso denegado. No eres el dueño de este mensaje.");
            }
            
            message.setTitle(updatedDetails.getTitle());
            message.setContent(updatedDetails.getContent());
            return messageRepository.save(message);
        }).orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
    }

    // DELETE: El owner borra (verificación de propiedad)
    public void delete(Long id, Long currentUserId) {
         messageRepository.findById(id).ifPresent(message -> {
            if (!message.getOwnerId().equals(currentUserId)) {
                throw new SecurityException("Acceso denegado para borrar este mensaje.");
            }
            messageRepository.delete(message);
        });
    }
}
