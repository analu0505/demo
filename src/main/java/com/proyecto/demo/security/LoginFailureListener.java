package com.proyecto.demo.security;

import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.service.AuditLogService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public LoginFailureListener(AuditLogService auditLogService,
                                UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        String email = (auth != null ? auth.getName() : "desconocido");

        User user = userRepository.findByEmail(email).orElse(null);
        Long userId = (user != null ? user.getId() : null);

        auditLogService.log(
                userId,
                "LOGIN_FAILED",
                "Login fallido para email=" + email
        );
    }
}
