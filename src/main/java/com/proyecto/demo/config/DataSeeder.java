package com.proyecto.demo.config;

import com.proyecto.demo.model.Role;
import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository users) {
        return args -> {
            if (users.count() == 0) {
                User admin = new User();
                admin.setEmail("admin@demo.com");
                admin.setPassword("admin123");               // simple para demo
                admin.setFullName("Administrador Demo");
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                users.save(admin);
            }
        };
    }
}
