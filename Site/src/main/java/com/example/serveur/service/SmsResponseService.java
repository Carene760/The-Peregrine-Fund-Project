package com.example.serveur.service;

import com.example.serveur.model.SmsResponse;
import com.example.serveur.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsResponseService {
    
    private final EncryptionUtil encryptionUtil;
    private final RestTemplate restTemplate;
    private final String expectedUsername;
    private final String expectedPassword;
    private final String internalSendUrl;

    public SmsResponseService(EncryptionUtil encryptionUtil,
                             RestTemplate restTemplate,
                             @Value("${gateway.auth.username}") String expectedUsername,
                             @Value("${gateway.auth.password}") String expectedPassword,
                             @Value("${gateway.internal-send-url}") String internalSendUrl) {
        this.encryptionUtil = encryptionUtil;
        this.restTemplate = restTemplate;
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
        this.internalSendUrl = internalSendUrl;
    }

    public SmsResponse createSuccessResponse() {
        SmsResponse response = new SmsResponse();
        response.setPayload(new SmsResponse.Payload());
        response.getPayload().setSuccess(true);
        return response;
    }

    public SmsResponse createErrorResponse(String error) {
        SmsResponse response = new SmsResponse();
        response.setPayload(new SmsResponse.Payload());
        response.getPayload().setSuccess(false);
        response.getPayload().setError(error);
        return response;
    }

    public void sendResponse(String phoneNumber, String message) {
        try {
            String messageChiffre = encryptionUtil.chiffrer(message);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumbers", Collections.singletonList(phoneNumber));
            requestBody.put("message", messageChiffre);
            requestBody.put("withDeliveryReport", true);

            String authStr = expectedUsername + ":" + expectedPassword;
            String base64Auth = Base64.getEncoder().encodeToString(authStr.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Auth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                internalSendUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Accusé de réception envoyé à: " + phoneNumber);
            } else {
                System.err.println("❌ Échec envoi accusé. Statut: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'accusé: " + e.getMessage());
        }
    }

    public void sendResponseSansChiffre(String phoneNumber, String message) {
        try {
            String messageChiffre = message;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumbers", Collections.singletonList(phoneNumber));
            requestBody.put("message", messageChiffre);
            requestBody.put("withDeliveryReport", true);

            String authStr = expectedUsername + ":" + expectedPassword;
            String base64Auth = Base64.getEncoder().encodeToString(authStr.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Auth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                internalSendUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Accusé de réception envoyé à: " + phoneNumber);
            } else {
                System.err.println("❌ Échec envoi accusé. Statut: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'accusé: " + e.getMessage());
        }
    }
}