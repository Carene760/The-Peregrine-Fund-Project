package com.example.serveur.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RememberUserInterceptor rememberUserInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebConfig(RememberUserInterceptor rememberUserInterceptor, ApiKeyInterceptor apiKeyInterceptor) {
        this.rememberUserInterceptor = rememberUserInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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
