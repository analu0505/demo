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
    private final MessageCryptoService crypto;

    public MessageService(MessageRepository msgRepo, UserRepository userRepo, MessageCryptoService crypto) {
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
        this.crypto = crypto;
    }

    //  PRUEBA: esto lista lo que está GUARDADO en DB (cifrado)
    public List<Message> listar() { return msgRepo.findAll(); }

    //  PRUEBA: esto lista lo que está GUARDADO en DB (cifrado)
    public List<Message> listarPorUsuario(Long usuarioId) {
        return msgRepo.findAllByUsuarioId(usuarioId);
    }
    //  Para que USER vea descifrado en pantalla (sin tocar DB)
public List<Message> listarPorUsuarioDescifrado(Long usuarioId) {
    List<Message> list = msgRepo.findAllByUsuarioId(usuarioId);
    for (Message m : list) {
        m.setContenido(crypto.decrypt(m.getContenido()));
    }
    return list;
}

//  Para ADMIN si querés que vea todo, pero cifrado (tal cual DB)
public List<Message> listarCifrado() {
    return msgRepo.findAll();
}


    // Para editar/ver descifrado (sin cambiar DB)
    public Message obtenerDescifrado(Long id) {
        Message m = msgRepo.findById(id).orElse(null);
        if (m == null) return null;
        m.setContenido(crypto.decrypt(m.getContenido()));
        return m;
    }

    public Message crear(Long usuarioId, Message m) {
        User u = userRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        m.setUsuario(u);

        //  CIFRAR antes de guardar
        m.setContenido(crypto.encrypt(m.getContenido()));

        return msgRepo.save(m);
    }

    public Message editar(Long id, Message m) {
        Message db = msgRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));

        db.setTitulo(m.getTitulo());

        //  CIFRAR antes de guardar
        db.setContenido(crypto.encrypt(m.getContenido()));

        // fechas se actualizan con @PreUpdate
        return msgRepo.save(db);
    }

    public void eliminar(Long id) { msgRepo.deleteById(id); }
}
