package com.example.serveur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
            .logoutUrl("/logout")                     // l’URL pour déclencher la déconnexion
            .logoutSuccessUrl("/")     // où rediriger après déconnexion
            .permitAll()
        );

        http
            .csrf(csrf -> csrf.disable()) // désactive CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/css/globals.css",
                    "/css/historique.css",
                    "/css/statistique.css",
                    "/css/style.css",
                    "/css/user.css",
                    "/images/**",
                    "/stat/**", 
                    "/history/**", 
                    "/login", 
                    "/alertes/**",
                    "/fonctions/**",
                    "/historique-message-status/**",
                    "/interventions/**",
                    "/messages/**",
                    "/messages-patrouilleurs/**",
                    "/patrouilleurs/**",
                    "/sites/**",
                    "/status-agents/**",
                    "/status-messages/**",
                    "/types-alerte/**",
                    "/users-app/**",
                    "/users/**",
                    "/api/**",
                    "/sync/download/**",
                    "/sync/upload/**",
                    "/sync/interventions/**",
                    "/sync/status/**",
                    "/sync/historique/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
