package com.example.serveur.controller;

import com.example.serveur.model.User;
import com.example.serveur.service.WebhookGatewayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/gateway/webhooks")
public class GatewayWebhookController {

    private final WebhookGatewayService webhookGatewayService;
    private final String expectedUsername;
    private final String expectedPassword;
    private final String defaultWebhookId;
    private final String defaultWebhookEvent;
    private final String defaultCallbackUrl;

    public GatewayWebhookController(WebhookGatewayService webhookGatewayService,
                                    @Value("${gateway.auth.username}") String expectedUsername,
                                    @Value("${gateway.auth.password}") String expectedPassword,
                                    @Value("${gateway.webhook.id}") String defaultWebhookId,
                                    @Value("${gateway.webhook.event}") String defaultWebhookEvent,
                                    @Value("${gateway.webhook.callback-url}") String defaultCallbackUrl) {
        this.webhookGatewayService = webhookGatewayService;
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
        this.defaultWebhookId = defaultWebhookId;
        this.defaultWebhookEvent = defaultWebhookEvent;
        this.defaultCallbackUrl = defaultCallbackUrl;
    }

    @GetMapping("/ui")
    public String webhookTestPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        model.addAttribute("webhookId", defaultWebhookId);
        model.addAttribute("webhookEvent", defaultWebhookEvent);
        model.addAttribute("webhookUrl", defaultCallbackUrl);
        return "gateway-webhooks";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createWebhook(@RequestBody(required = false) Map<String, String> payload,
                                                             HttpSession session,
                                                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthorized(session, authorization)) {
            return forbidden("Action reservee a l administrateur");
        }

        Map<String, String> safePayload = payload == null ? Map.of() : payload;
        WebhookGatewayService.WebhookOperationResult result = webhookGatewayService.createWebhook(
                safePayload.get("id"),
                safePayload.get("url"),
                safePayload.get("event")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        response.put("webhookId", result.webhookId());
        response.put("gatewayResponse", result.gatewayResponse());

        return ResponseEntity.status(result.status()).body(response);
    }

    @PostMapping("/default")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createDefaultWebhook(HttpSession session,
                                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        return createWebhook(Map.of(), session, authorization);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWebhook(@PathVariable String id,
                                                             HttpSession session,
                                                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthorized(session, authorization)) {
            return forbidden("Action reservee a l administrateur");
        }

        WebhookGatewayService.WebhookOperationResult result = webhookGatewayService.deleteWebhook(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        response.put("webhookId", id);
        response.put("gatewayResponse", result.gatewayResponse());

        return ResponseEntity.status(result.status()).body(response);
    }

    private boolean isAuthorized(HttpSession session, String authorization) {
        return isAdmin(session) || isBasicAuthValid(authorization);
    }

    private boolean isAdmin(HttpSession session) {
        Object userObj = session.getAttribute("currentUser");
        if (!(userObj instanceof User user)) {
            return false;
        }
        return user.getFonction() != null
                && user.getFonction().getFonction() != null
                && "Administrateur".equalsIgnoreCase(user.getFonction().getFonction());
    }

    private boolean isBasicAuthValid(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }

        try {
            String base64Credentials = authorization.substring("Basic ".length()).trim();
            String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            int separatorIndex = decoded.indexOf(':');
            if (separatorIndex < 0) {
                return false;
            }

            String username = decoded.substring(0, separatorIndex);
            String password = decoded.substring(separatorIndex + 1);
            return expectedUsername.equals(username) && expectedPassword.equals(password);
        } catch (Exception e) {
            return false;
        }
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(403).body(response);
    }
}
