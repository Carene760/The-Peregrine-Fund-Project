package com.example.serveur.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Authenticates machine-facing endpoints (/api/**, /sync/**) that cannot do
 * an interactive login: the Android field app and the SMSSync SMS gateway.
 *
 * These routes used to be fully open (permitAll() with zero credential
 * check). This interceptor requires ONE of:
 *  - header "X-API-Key: <app.api.key>" (what the Android app now sends,
 *    see ConfigLoader/ApiService on the app side), or
 *  - HTTP Basic auth with the existing gateway.auth.username/password
 *    (already used elsewhere for the gateway's own webhook-management API;
 *    reused here so the SMSSync gateway device can be configured with the
 *    same credentials for its webhook callback, since it cannot send
 *    arbitrary custom headers as easily as the Android app can).
 *
 * This is intentionally a single shared secret per client type, not
 * per-user auth: these are service-to-service calls, not human logins.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final String expectedApiKey;
    private final String gatewayUsername;
    private final String gatewayPassword;

    public ApiKeyInterceptor(@Value("${app.api.key}") String expectedApiKey,
                              @Value("${gateway.auth.username}") String gatewayUsername,
                              @Value("${gateway.auth.password}") String gatewayPassword) {
        this.expectedApiKey = expectedApiKey;
        this.gatewayUsername = gatewayUsername;
        this.gatewayPassword = gatewayPassword;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isApiKeyValid(request) || isBasicAuthValid(request)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Missing or invalid API credentials\"}");
        return false;
    }

    private boolean isApiKeyValid(HttpServletRequest request) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            return false;
        }
        String provided = request.getHeader(API_KEY_HEADER);
        return expectedApiKey.equals(provided);
    }

    private boolean isBasicAuthValid(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }
        try {
            String decoded = new String(
                    Base64.getDecoder().decode(authorization.substring("Basic ".length()).trim()),
                    StandardCharsets.UTF_8
            );
            int separator = decoded.indexOf(':');
            if (separator < 0) {
                return false;
            }
            String username = decoded.substring(0, separator);
            String password = decoded.substring(separator + 1);
            return gatewayUsername != null && gatewayUsername.equals(username)
                    && gatewayPassword != null && gatewayPassword.equals(password);
        } catch (Exception e) {
            return false;
        }
    }
}
