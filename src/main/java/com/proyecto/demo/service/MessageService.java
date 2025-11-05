package com.proyecto.demo.service;

import com.proyecto.demo.model.Message;
import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.MessageRepository;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageService {

    private final MessageRepository msgRepo;
    private final UserRepository userRepo;

    public MessageService(MessageRepository msgRepo, UserRepository userRepo) {
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
    }

    public List<Message> listar() { return msgRepo.findAll(); }

    public List<Message> listarPorUsuario(Long usuarioId) {
        return msgRepo.findAllByUsuarioId(usuarioId);
    }

    public Message obtener(Long id) {
        return msgRepo.findById(id).orElse(null);
    }

    public Message crear(Long usuarioId, Message m) {
        User u = userRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        m.setUsuario(u);
        return msgRepo.save(m);
    }

    public Message editar(Long id, Message m) {
        Message db = msgRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        db.setTitulo(m.getTitulo());
        db.setContenido(m.getContenido());
        // fechas se actualizan con @PreUpdate
        return msgRepo.save(db);
    }

    public void eliminar(Long id) { msgRepo.deleteById(id); }
}
