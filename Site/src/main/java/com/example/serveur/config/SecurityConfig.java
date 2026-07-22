package com.example.serveur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt encoder used to hash and verify all stored passwords
     * (web admin users in User_ and mobile app users in userapp).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
            .logoutUrl("/logout")                     // l’URL pour déclencher la déconnexion
            .invalidateHttpSession(true)
            .deleteCookies("rememberUserEmail", "JSESSIONID")
            .logoutSuccessUrl("/")     // où rediriger après déconnexion
            .permitAll()
        );

        // NOTE (security follow-up, documented not fully closed tonight):
        // - "/api/**" and "/sync/**" stay permitAll() *at the Spring Security
        //   filter chain level* on purpose: these are machine-facing routes
        //   (Android app + SMS gateway) that cannot do an interactive login,
        //   so they never populate Spring Security's own SecurityContext.
        //   They are NOT actually open: ApiKeyInterceptor (see WebConfig)
        //   requires a shared "X-API-Key" header (or gateway Basic auth) on
        //   every request to these paths and rejects with 401 otherwise.
        // - The admin/web routes below also stay permitAll() here because
        //   authentication for them is enforced by RememberUserInterceptor
        //   (session + HMAC-signed remember-me cookie, see
        //   RememberMeCookieSigner), not by Spring Security's own
        //   authentication mechanism. Migrating these to a real Spring
        //   Security formLogin()/UserDetailsService flow (with CSRF
        //   re-enabled) is the recommended next step, but every admin
        //   Thymeleaf form would need auditing for a CSRF token first to
        //   avoid breaking submissions - left as a follow-up, see
        //   SECURITY_REMEDIATION.md.
        http
            .csrf(csrf -> csrf.disable()) // désactive CSRF - voir note ci-dessus
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
                    "/admin/gateway/webhooks/**",
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
