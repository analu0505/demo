package com.proyecto.demo.security;

import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.service.AuditLogService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public LoginSuccessListener(AuditLogService auditLogService,
                                UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email).orElse(null);
        Long userId = (user != null ? user.getId() : null);

        auditLogService.log(
                userId,
                "LOGIN_SUCCESS",
                "Login correcto para email=" + email
        );
    }
}
