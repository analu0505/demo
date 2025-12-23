package com.proyecto.demo.web;

import com.proyecto.demo.model.AuditLog;
import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuditController {

    private final AuditLogService auditService;
    private final UserRepository userRepo;

    public AuditController(AuditLogService auditService, UserRepository userRepo) {
        this.auditService = auditService;
        this.userRepo = userRepo;
    }

    @GetMapping("/audit")
    public String list(Model model) {

        List<AuditLog> logs = auditService.listar();

        // Map userId -> email (para no reventar el Thymeleaf)
        Map<Long, String> userEmails = new HashMap<>();
        for (AuditLog l : logs) {
            if (l.getUserId() != null && !userEmails.containsKey(l.getUserId())) {
                User u = userRepo.findById(l.getUserId()).orElse(null);
                userEmails.put(l.getUserId(), u != null ? u.getEmail() : "N/A");
            }
        }

        model.addAttribute("logs", logs);
        model.addAttribute("userEmails", userEmails);

        return "audit/list";
    }
}

