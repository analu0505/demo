package com.proyecto.demo.service;

import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // Inyección por constructor
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ====== CRUD ======
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // Utilidad para validar duplicados por email
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
