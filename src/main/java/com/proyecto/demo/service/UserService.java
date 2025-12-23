package com.proyecto.demo.service;

import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    // ✅ para validación al crear usuario
    public boolean emailExists(String email) {
        return repo.existsByEmail(email);
    }

    // ✅ para validación al editar usuario (ignora el mismo id)
    public boolean emailExistsForOtherUser(String email, Long currentUserId) {
        User u = repo.findByEmail(email).orElse(null);
        return u != null && !u.getId().equals(currentUserId);
    }

    public User save(User u) {
        return repo.save(u);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
