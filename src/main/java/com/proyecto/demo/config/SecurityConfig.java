package com.proyecto.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserDetailsService userDetailsService) throws Exception {

http
    .userDetailsService(userDetailsService)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/login", "/h2-console/**", "/access-denied").permitAll()
        .requestMatchers("/users/**").hasRole("ADMIN")
        .requestMatchers("/vault/**").hasRole("USER")
        .requestMatchers("/messages/**").hasAnyRole("ADMIN", "USER")
        .anyRequest().authenticated()
    )
    .formLogin(form -> form
        .loginPage("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .defaultSuccessUrl("/messages", true)
        .failureUrl("/login?error=true")
        .permitAll()
    )
    .logout(logout -> logout
        // permitir cerrar sesión con GET /logout (para usar un <a href="/logout">)
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
        .logoutSuccessUrl("/login?logout=true")
        .permitAll()
    )

    
    .exceptionHandling(ex -> ex
        .accessDeniedPage("/access-denied")   // <<--- NUEVO
    )
    .csrf(csrf -> csrf
        .ignoringRequestMatchers("/h2-console/**")
    )
    .headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin())
    );

        return http.build();
    }
}
