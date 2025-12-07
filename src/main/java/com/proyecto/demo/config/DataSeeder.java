package com.proyecto.demo.config;

import com.proyecto.demo.model.Role;
import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                User admin = new User();
                admin.setEmail("admin@demo.com");
                admin.setPassword(encoder.encode("admin123"));   // <<--- CIFRADO
                admin.setFullName("Administrador SafeBox");
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin.setCreatedAt(now);
                admin.setUpdatedAt(now);
                users.save(admin);

                User user = new User();
                user.setEmail("user@demo.com");
                user.setPassword(encoder.encode("user123"));     // <<--- CIFRADO
                user.setFullName("Usuario Demo");
                user.setRole(Role.USER);
                user.setEnabled(true);
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                users.save(user);
            }
        };
    }
}
