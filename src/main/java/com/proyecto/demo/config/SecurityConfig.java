package com.proyecto.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // H2 console
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            .authorizeHttpRequests(auth -> auth
                // Públicos
                .requestMatchers("/login", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // ADMIN
                .requestMatchers("/users", "/users/**", "/audit", "/audit/**", "/reports", "/reports/**").hasRole("ADMIN")


                // USER y ADMIN
                .requestMatchers("/messages/**", "/vault/**").hasAnyRole("USER", "ADMIN")

                .anyRequest().authenticated()
            )

            // ✅ Página de acceso denegado (tu template /error/access-denied.html)
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/messages", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

