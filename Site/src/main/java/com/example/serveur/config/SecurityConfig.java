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
            .invalidateHttpSession(true)
            .deleteCookies("rememberUserEmail", "JSESSIONID")
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
                    "/js/**",
                    "/images/**",
                    "/stat/**", 
                    "/history/**", 
                    "/evenements/**",
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
                    "/userslist",
                    "/agentslist",
                    "/api/**",
                    "/sync/download/**",
                    "/sync/upload/**",
                    "/sync/interventions/**",
                    "/sync/status/**",
                    "/sync/evenements/**",
                    "/sync/historique/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
