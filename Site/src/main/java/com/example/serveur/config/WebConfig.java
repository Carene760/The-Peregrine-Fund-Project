package com.example.serveur.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RememberUserInterceptor rememberUserInterceptor;

    public WebConfig(RememberUserInterceptor rememberUserInterceptor) {
        this.rememberUserInterceptor = rememberUserInterceptor;
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
    }
}
