package com.proyecto.demo.web;

import com.proyecto.demo.model.Message;
import com.proyecto.demo.model.Role;
import com.proyecto.demo.model.User;
import com.proyecto.demo.service.MessageService;
import com.proyecto.demo.service.UserService;
import org.springframework.security.core.Authentication;
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

    //  LISTA: ADMIN ve cifrado / USER ve solo lo suyo descifrado
    @GetMapping("/messages")
    public String list(Authentication auth, Model model) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        if (isAdmin) {
            model.addAttribute("mensajes", messageService.listarCifrado());
        } else {
            model.addAttribute("mensajes", messageService.listarPorUsuarioDescifrado(current.getId()));
        }

        model.addAttribute("isAdmin", isAdmin);
        return "messages/list";
    }
     //FORM NUEVO 
@GetMapping("/messages/new")
public String createForm(Authentication auth, Model model) {
    User current = userService.findByEmail(auth.getName());
    boolean isAdmin = current.getRole() == Role.ADMIN;

    model.addAttribute("message", new Message());

    // ADMIN puede elegir usuario, USER no
    model.addAttribute("usuarios", isAdmin ? userService.findAll() : null);
    model.addAttribute("usuarioId", isAdmin ? null : current.getId());
    model.addAttribute("isAdmin", isAdmin);

    return "messages/form";
}


    // GUARDAR NUEVO
    @PostMapping("/messages")
    public String create(Authentication auth,
                         @ModelAttribute("message") Message message,
                         @RequestParam("usuarioId") Long usuarioId) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        //  USER solo puede crear para sí mismo
        if (!isAdmin && !usuarioId.equals(current.getId())) {
            return "redirect:/access-denied";
        }

        messageService.crear(usuarioId, message);
        return "redirect:/messages";
    }

    // FORM EDITAR (sigue trayendo descifrado para el form)
    @GetMapping("/messages/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        Message m = messageService.obtenerDescifrado(id);
        if (m == null) throw new IllegalArgumentException("Mensaje no encontrado: " + id);

        //  USER solo edita lo suyo
        if (!isAdmin && (m.getUsuario() == null || !m.getUsuario().getId().equals(current.getId()))) {
            return "redirect:/access-denied";
        }

        model.addAttribute("message", m);
        model.addAttribute("usuarios", isAdmin ? userService.findAll() : null);
        model.addAttribute("usuarioId", m.getUsuario().getId());
        model.addAttribute("isAdmin", isAdmin);

        return "messages/form";
    }

    // ACTUALIZAR
    @PostMapping("/messages/{id}")
    public String update(@PathVariable Long id,
                         Authentication auth,
                         @ModelAttribute("message") Message message) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        Message existing = messageService.obtenerDescifrado(id);
        if (existing == null) throw new IllegalArgumentException("Mensaje no encontrado: " + id);

        //  USER solo edita lo suyo
        if (!isAdmin && (existing.getUsuario() == null || !existing.getUsuario().getId().equals(current.getId()))) {
            return "redirect:/access-denied";
        }

        messageService.editar(id, message);
        return "redirect:/messages";
    }

    // ELIMINAR
    @PostMapping("/messages/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        Message existing = messageService.obtenerDescifrado(id);
        if (existing == null) throw new IllegalArgumentException("Mensaje no encontrado: " + id);

        // ✅ USER solo borra lo suyo
        if (!isAdmin && (existing.getUsuario() == null || !existing.getUsuario().getId().equals(current.getId()))) {
            return "redirect:/access-denied";
        }

        messageService.eliminar(id);
        return "redirect:/messages";
    }
}
