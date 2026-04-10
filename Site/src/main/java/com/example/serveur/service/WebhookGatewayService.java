package com.example.serveur.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebhookGatewayService {

    private final RestTemplate restTemplate;
    private final String gatewayBaseUrl;
    private final String createPath;
    private final String deletePath;
    private final String defaultWebhookId;
    private final String defaultWebhookEvent;
    private final String defaultCallbackUrl;
    private final String authUsername;
    private final String authPassword;

    private final Set<String> knownWebhookIds = ConcurrentHashMap.newKeySet();

    public WebhookGatewayService(RestTemplate restTemplate,
                                 @Value("${gateway.base-url}") String gatewayBaseUrl,
                                 @Value("${gateway.webhook.create-path}") String createPath,
                                 @Value("${gateway.webhook.delete-path}") String deletePath,
                                 @Value("${gateway.webhook.id}") String defaultWebhookId,
                                 @Value("${gateway.webhook.event}") String defaultWebhookEvent,
                                 @Value("${gateway.webhook.callback-url}") String defaultCallbackUrl,
                                 @Value("${gateway.auth.username}") String authUsername,
                                 @Value("${gateway.auth.password}") String authPassword) {
        this.restTemplate = restTemplate;
        this.gatewayBaseUrl = gatewayBaseUrl;
        this.createPath = createPath;
        this.deletePath = deletePath;
        this.defaultWebhookId = defaultWebhookId;
        this.defaultWebhookEvent = defaultWebhookEvent;
        this.defaultCallbackUrl = defaultCallbackUrl;
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }

    public WebhookOperationResult createWebhook(String id, String callbackUrl, String event) {
        String resolvedId = valueOrDefault(id, defaultWebhookId);
        String resolvedCallback = valueOrDefault(callbackUrl, defaultCallbackUrl);
        String resolvedEvent = valueOrDefault(event, defaultWebhookEvent);

        if (resolvedId.isBlank() || resolvedCallback.isBlank() || resolvedEvent.isBlank()) {
            return WebhookOperationResult.failure(HttpStatus.BAD_REQUEST, "Parametres webhook incomplets");
        }

        if (knownWebhookIds.contains(resolvedId)) {
            return WebhookOperationResult.failure(HttpStatus.CONFLICT, "Webhook deja connu pour id=" + resolvedId);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", resolvedId);
        payload.put("url", resolvedCallback);
        payload.put("event", resolvedEvent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, buildHeaders());
        String url = joinUrl(gatewayBaseUrl, createPath);

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    knownWebhookIds.add(resolvedId);
                    return WebhookOperationResult.success(response.getStatusCode(), "Webhook cree", resolvedId, response.getBody());
                }
                if (attempt == 2) {
                    return WebhookOperationResult.failure(response.getStatusCode(), "Echec creation webhook");
                }
            } catch (Exception exception) {
                if (attempt == 2) {
                    return WebhookOperationResult.failure(HttpStatus.BAD_GATEWAY,
                            "Erreur gateway creation webhook: " + exception.getMessage());
                }
            }
        }

        return WebhookOperationResult.failure(HttpStatus.BAD_GATEWAY, "Echec creation webhook");
    }

    public WebhookOperationResult deleteWebhook(String id) {
        String resolvedId = valueOrDefault(id, defaultWebhookId);
        if (resolvedId.isBlank()) {
            return WebhookOperationResult.failure(HttpStatus.BAD_REQUEST, "ID webhook manquant");
        }

        String path = deletePath.replace("{id}", resolvedId);
        String url = joinUrl(gatewayBaseUrl, path);
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders());

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    knownWebhookIds.remove(resolvedId);
                    return WebhookOperationResult.success(response.getStatusCode(), "Webhook supprime", resolvedId, response.getBody());
                }
                if (attempt == 2) {
                    return WebhookOperationResult.failure(response.getStatusCode(), "Echec suppression webhook");
                }
            } catch (Exception exception) {
                if (attempt == 2) {
                    return WebhookOperationResult.failure(HttpStatus.BAD_GATEWAY,
                            "Erreur gateway suppression webhook: " + exception.getMessage());
                }
            }
        }

        return WebhookOperationResult.failure(HttpStatus.BAD_GATEWAY, "Echec suppression webhook");
    }

    private HttpHeaders buildHeaders() {
        String credentials = authUsername + ":" + authPassword;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        return headers;
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue == null ? "" : defaultValue.trim();
        }
        return value.trim();
    }

    public record WebhookOperationResult(boolean success,
                                         HttpStatusCode status,
                                         String message,
                                         String webhookId,
                                         String gatewayResponse) {
        public static WebhookOperationResult success(HttpStatusCode status,
                                                     String message,
                                                     String webhookId,
                                                     String gatewayResponse) {
            return new WebhookOperationResult(true, status, message, webhookId, gatewayResponse);
        }

        public static WebhookOperationResult failure(HttpStatusCode status, String message) {
            return new WebhookOperationResult(false, status, message, null, null);
        }
    }
}
