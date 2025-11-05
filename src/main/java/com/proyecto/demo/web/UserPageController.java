package com.proyecto.demo.web;

import com.proyecto.demo.model.User;
import com.proyecto.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class UserPageController {

    private final UserService userService;

    public UserPageController(UserService userService) {
        this.userService = userService;
    }

    // LISTA
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
    public String create(@ModelAttribute User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userService.save(user);
        return "redirect:/users";
    }

    // FORM EDITAR
    @GetMapping("/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User u = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        model.addAttribute("user", u);
        model.addAttribute("title", "Editar usuario");
        return "users/form";
    }

    // ACTUALIZAR
    @PostMapping("/users/{id}")
    public String update(@PathVariable Long id, @ModelAttribute User user) {
        User existente = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        existente.setEmail(user.getEmail());
        existente.setFullName(user.getFullName());
        existente.setPassword(user.getPassword());
        existente.setRole(user.getRole());
        existente.setEnabled(user.isEnabled());
        existente.setUpdatedAt(LocalDateTime.now());

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
