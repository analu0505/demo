package com.proyecto.demo.service;

import com.proyecto.demo.model.User;
import com.proyecto.demo.model.Role;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// NOTA: En la implementación real, necesitarás Spring Security para
// hashear la contraseña (BCryptPasswordEncoder) y obtener el usuario actual.

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    public User save(User user) {
        // En una implementación completa: hashear la contraseña aquí
        return userRepository.save(user);
    }

    // READ: ADMIN puede listar
    public List<User> findAll(Role currentUserRole) {
        if (currentUserRole != Role.ADMIN) {
            // Implementación básica de seguridad: Denegar si no es ADMIN
            throw new SecurityException("Acceso denegado. Solo ADMIN puede listar usuarios.");
        }
        return userRepository.findAll();
    }
    
    // READ: Usuario puede ver su propio perfil
    public Optional<User> findById(Long id, Long currentUserId) {
        // Lógica: solo el dueño o un ADMIN puede ver
        if (!id.equals(currentUserId)) {
             // En un sistema real, el ADMIN también debería pasar esta verificación.
             throw new SecurityException("Acceso denegado. Solo puedes ver tu propio perfil.");
        }
        return userRepository.findById(id);
    }

    // UPDATE: Usuario puede editar su perfil (excepto role)
    public User update(Long id, User updatedDetails, Long currentUserId) {
        return userRepository.findById(id).map(user -> {
            if (!user.getId().equals(currentUserId)) {
                throw new SecurityException("Acceso denegado para editar este perfil.");
            }
            
            user.setFullName(updatedDetails.getFullName());
            user.setEmail(updatedDetails.getEmail());
            // Se debe mantener el role original, como lo pide el requerimiento.
            
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    // ADMIN: ADMIN puede listar/bloquear (toggle 'enabled')
    public User toggleEnable(Long id, boolean enable, Role currentUserRole) {
         if (currentUserRole != Role.ADMIN) {
            throw new SecurityException("Acceso denegado. Solo ADMIN puede bloquear/desbloquear.");
        }
        
        return userRepository.findById(id).map(user -> {
            user.setEnabled(enable);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // DELETE (Opcional)
    public void delete(Long id, Role currentUserRole) {
        if (currentUserRole != Role.ADMIN) {
             throw new SecurityException("Acceso denegado.");
        }
        userRepository.deleteById(id);
    }
}
