
package com.proyecto.demo.web;

import com.proyecto.demo.model.User;
import com.proyecto.demo.model.VaultItem;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.service.VaultItemService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class VaultController {

    private final VaultItemService vaultService;
    private final UserRepository userRepo;

    public VaultController(VaultItemService vaultService,
                           UserRepository userRepo) {
        this.vaultService = vaultService;
        this.userRepo = userRepo;
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
    }

    @GetMapping("/vault")
    public String list(Authentication auth, Model model) {
        User current = getCurrentUser(auth);
        model.addAttribute("items", vaultService.listarPorUsuario(current.getId()));
        return "vault/list";
    }

    @GetMapping("/vault/new")
    public String newForm(Model model) {
        model.addAttribute("item", new VaultItem());
        return "vault/form";
    }

    @PostMapping("/vault")
    public String create(@ModelAttribute VaultItem item, Authentication auth) {
        User current = getCurrentUser(auth);
        vaultService.crear(current.getId(), item);
        return "redirect:/vault";
    }

    @GetMapping("/vault/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        User current = getCurrentUser(auth);
        VaultItem item = vaultService.obtener(id);
        if (item == null || !item.getOwner().getId().equals(current.getId())) {
            throw new IllegalStateException("No tiene permiso para editar este item");
        }

        // no mostramos el contenido descifrado aquí
        model.addAttribute("item", item);
        return "vault/form";
    }

    @PostMapping("/vault/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute VaultItem item,
                         Authentication auth) {

        User current = getCurrentUser(auth);
        vaultService.editar(current.getId(), id, item);
        return "redirect:/vault";
    }

    @PostMapping("/vault/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        User current = getCurrentUser(auth);
        vaultService.eliminar(current.getId(), id);
        return "redirect:/vault";
    }

    @GetMapping("/vault/{id}/view")
    public String viewDecrypted(@PathVariable Long id,
                                Authentication auth,
                                Model model) {

        User current = getCurrentUser(auth);
        String contenido = vaultService.descifrarContenido(current.getId(), id);
        VaultItem item = vaultService.obtener(id);
        model.addAttribute("titulo", item.getTitulo());
        model.addAttribute("contenido", contenido);
        return "vault/view";
    }
}
