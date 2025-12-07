package com.proyecto.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o usuario deshabilitado");
        }
        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada correctamente");
        }
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Acceso denegado");
        return "error/access-denied";
    }
}
