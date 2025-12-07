package com.proyecto.demo.web;

import com.proyecto.demo.model.Message;
import com.proyecto.demo.service.MessageService;
import com.proyecto.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MessagePageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessagePageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    // LISTA
    @GetMapping("/messages")
    public String list(Model model) {
        model.addAttribute("mensajes", messageService.listar());
        return "messages/list";
    }

    // FORM NUEVO
    @GetMapping("/messages/new")
    public String createForm(Model model) {
        model.addAttribute("message", new Message());
        model.addAttribute("usuarios", userService.findAll());
        model.addAttribute("usuarioId", null);
        return "messages/form";
    }

    // GUARDAR NUEVO
    @PostMapping("/messages")
    public String create(@ModelAttribute("message") Message message,
                         @RequestParam("usuarioId") Long usuarioId) {

        messageService.crear(usuarioId, message);
        return "redirect:/messages";
    }

    // FORM EDITAR
    @GetMapping("/messages/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Message m = messageService.obtener(id);
        if (m == null) {
            throw new IllegalArgumentException("Mensaje no encontrado: " + id);
        }
        model.addAttribute("message", m);
        model.addAttribute("usuarios", userService.findAll());
        model.addAttribute("usuarioId", m.getUsuario().getId());
        return "messages/form";
    }

    // ACTUALIZAR
    @PostMapping("/messages/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("message") Message message) {

        messageService.editar(id, message);
        return "redirect:/messages";
    }

    // ELIMINAR
    @PostMapping("/messages/{id}/delete")
    public String delete(@PathVariable Long id) {
        messageService.eliminar(id);
        return "redirect:/messages";
    }
}
