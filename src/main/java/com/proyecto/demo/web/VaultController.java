package com.proyecto.demo.web;

import com.proyecto.demo.model.Role;
import com.proyecto.demo.model.User;
import com.proyecto.demo.model.VaultItem;
import com.proyecto.demo.service.UserService;
import com.proyecto.demo.service.VaultItemService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class VaultController {

    private final VaultItemService vaultItemService;
    private final UserService userService;

    public VaultController(VaultItemService vaultItemService, UserService userService) {
        this.vaultItemService = vaultItemService;
        this.userService = userService;
    }

    // LISTA / HOME
    @GetMapping("/vault")
    public String vaultHome(@RequestParam(value = "ownerId", required = false) Long ownerId,
                            Authentication auth,
                            Model model) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        // Lista de usuarios (solo ADMIN)
        if (isAdmin) {
            model.addAttribute("usuarios", userService.findAll());
        }

        // Si ADMIN selecciona a quién ver
        if (isAdmin && ownerId != null) {
            model.addAttribute("ownerSeleccionado", ownerId);
            model.addAttribute("items", vaultItemService.listarPorOwner(ownerId));
            model.addAttribute("viendoDeOtro", !ownerId.equals(current.getId()));
            return "vault/list";
        }

        // Normal: cada usuario ve lo suyo
        model.addAttribute("ownerSeleccionado", current.getId());
        model.addAttribute("items", vaultItemService.listarPorOwner(current.getId()));
        model.addAttribute("viendoDeOtro", false);
        return "vault/list";
    }

    // FORM NUEVO ITEM
    @GetMapping("/vault/new")
    public String newItem(@RequestParam("ownerId") Long ownerId,
                          Authentication auth,
                          Model model) {

        User current = userService.findByEmail(auth.getName());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        // USER solo puede crear en su propia bóveda
        if (!isAdmin && !ownerId.equals(current.getId())) {
            return "redirect:/vault";
        }

        model.addAttribute("ownerId", ownerId);
        model.addAttribute("item", new VaultItem());
        return "vault/form";
    }

    // GUARDAR NUEVO ITEM
    @PostMapping("/vault")
    public String createItem(@RequestParam("ownerId") Long ownerId,
                             @ModelAttribute("item") VaultItem item,
                             Authentication auth) {

        vaultItemService.crear(ownerId, item, auth);
        return "redirect:/vault?ownerId=" + ownerId;
    }

    // ELIMINAR ITEM
    @PostMapping("/vault/{id}/delete")
    public String deleteItem(@PathVariable Long id,
                             @RequestParam("ownerId") Long ownerId,
                             Authentication auth) {

        vaultItemService.eliminar(id, auth);
        return "redirect:/vault?ownerId=" + ownerId;
    }
}
