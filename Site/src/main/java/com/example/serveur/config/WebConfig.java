package com.example.serveur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RememberUserInterceptor rememberUserInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebConfig(RememberUserInterceptor rememberUserInterceptor, ApiKeyInterceptor apiKeyInterceptor) {
        this.rememberUserInterceptor = rememberUserInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    /**
     * Français par défaut, mémorisé via cookie (pas la session) pour que le
     * choix de langue survive à la déconnexion/reconnexion - cf.
     * templates/fragments/language-switcher.html (?lang=fr|en|mg).
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("app-locale");
        resolver.setDefaultLocale(Locale.FRENCH);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());

        registry.addInterceptor(rememberUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/images/**",
                        "/api/**",
                        "/sync/**"
                );

        // Machine-facing endpoints (Android app + SMS gateway) require a
        // shared API key / Basic auth instead of the interactive session
        // login handled by rememberUserInterceptor above.
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/**", "/sync/**");
    }
}
