package com.proyecto.demo.web;

import com.proyecto.demo.model.User;
import com.proyecto.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class UserPageController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserPageController(UserService userService,
                              PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // LISTA (solo ADMIN, SecurityConfig lo protege)
    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    // FORM NUEVO
    @GetMapping("/users/new")
    public String createForm(Model model) {
        User u = new User();
        u.setEnabled(true);
        model.addAttribute("user", u);
        model.addAttribute("title", "Nuevo usuario");
        return "users/form";
    }

    // GUARDAR NUEVO
    @PostMapping("/users")
    public String create(@ModelAttribute User user, Model model) {

        // Validar email duplicado
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("user", user);
            model.addAttribute("title", "Nuevo usuario");
            model.addAttribute("emailError", "El email ya está registrado");
            return "users/form";
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // cifrar password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userService.save(user);
        return "redirect:/users";
    }

    // FORM EDITAR
    @GetMapping("/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        // ✅ findById devuelve User, no Optional
        User u = userService.findById(id);
        if (u == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + id);
        }

        // No mostrar el hash en el form
        u.setPassword("");
        model.addAttribute("user", u);
        model.addAttribute("title", "Editar usuario");
        return "users/form";
    }

    // ACTUALIZAR
    @PostMapping("/users/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute User user,
                         Model model) {

        // ✅ findById devuelve User, no Optional
        User existente = userService.findById(id);
        if (existente == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + id);
        }

        // Validar email duplicado contra otros usuarios
        if (userService.emailExistsForOtherUser(user.getEmail(), id)) {
            user.setId(id);
            model.addAttribute("user", user);
            model.addAttribute("title", "Editar usuario");
            model.addAttribute("emailError", "El email ya está registrado por otro usuario");
            return "users/form";
        }

        existente.setEmail(user.getEmail());
        existente.setFullName(user.getFullName());
        existente.setRole(user.getRole());
        existente.setEnabled(user.isEnabled());
        existente.setUpdatedAt(LocalDateTime.now());

        // si el campo password viene vacío, no se cambia
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.save(existente);
        return "redirect:/users";
    }

    // ELIMINAR
    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }
}

