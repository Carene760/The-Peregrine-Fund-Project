package com.example.serveur.controller;

import com.example.serveur.model.SmsResponse;
import com.example.serveur.service.SmsLoggingService;
import com.example.serveur.service.SmsProcessingService;
import com.example.serveur.service.SmsResponseService;
import com.example.serveur.service.AllowedNumbersService;
import com.example.serveur.service.AlerteService;
import com.example.serveur.service.SiteService;
import com.example.serveur.service.HistoriqueMessageStatusService;

import jakarta.annotation.PostConstruct;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.serveur.util.*;

@RestController
@RequestMapping("/api")
public class ServeurController {

    private final SmsProcessingService smsProcessingService;
    private final SmsResponseService smsResponseService;
    private final SmsLoggingService smsLoggingService;
    private final AllowedNumbersService allowedNumbersService;
    private final AlerteService alerteService;
    private final SiteService siteService; // ← Nouveau service
    private final HistoriqueMessageStatusService historiqueMessageStatusService;

    public ServeurController(SmsProcessingService smsProcessingService,
                            SmsResponseService smsResponseService,
                            SmsLoggingService smsLoggingService,
                            AllowedNumbersService allowedNumbersService,
                            AlerteService alerteService,
                            SiteService siteService,
                            HistoriqueMessageStatusService historiqueMessageStatusService) {
        this.smsProcessingService = smsProcessingService;
        this.smsResponseService = smsResponseService;
        this.smsLoggingService = smsLoggingService;
        this.allowedNumbersService = allowedNumbersService;
        this.alerteService = alerteService;
        this.siteService = siteService;
        this.historiqueMessageStatusService = historiqueMessageStatusService;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ Serveur démarré avec " + allowedNumbersService.getNumerosAutorises().size() + " numéros autorisés");
    }

    @GetMapping("/test-chiffrement")
    public String testChiffrement(@RequestParam String message) {
        EncryptionUtil encryptionUtil= new EncryptionUtil("0123456789abcdef");

        try {
            String dechiffre = encryptionUtil.dechiffrer(message);
            String chiffre = encryptionUtil.chiffrer(dechiffre);
            
            return String.format("""
                Original: %s
                Chiffré: %s
                Déchiffré: %s
                Match: %s
                Longueur original: %d
                Longueur chiffré: %d
                """, 
                message, chiffre, dechiffre, 
                message.equals(dechiffre),
                message.length(), chiffre.length());
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }

    @GetMapping("/test-dechiffrement")
    public String testDeChiffrement(@RequestParam String message) {
        EncryptionUtil encryptionUtil= new EncryptionUtil("0123456789abcdef");

        try {
            String chiffre = encryptionUtil.chiffrer(message);
            String dechiffre = encryptionUtil.dechiffrer(chiffre);
            
            return String.format("""
                Original: %s
                Chiffré: %s
                Déchiffré: %s
                Match: %s
                Longueur original: %d
                Longueur chiffré: %d
                """, 
                message, chiffre, dechiffre, 
                message.equals(dechiffre),
                message.length(), chiffre.length());
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }

    // ==================== ENDPOINT SMS WEBHOOK (EXISTANT) ====================
    @PostMapping("/webhook")
    public SmsResponse handleSmsWebhook(@RequestBody GatewayWebhookRequest webhookRequest) throws Exception {
        if (!"sms:received".equals(webhookRequest.getEvent())) {
            return smsResponseService.createErrorResponse("Event non supporté");
        }

        GatewayWebhookRequest.Payload payload = webhookRequest.getPayload();
        String phoneNumber = payload.getPhoneNumber();
        String messageChiffre = payload.getMessage();

        if (!allowedNumbersService.isNumeroAutorise(phoneNumber)) {
            System.out.println("🚫 SMS ignoré de: " + phoneNumber);
            return smsResponseService.createSuccessResponse();
        }

        String messageClair = smsProcessingService.processMessage(messageChiffre, phoneNumber);
        String reponseAccuse = traiterMessage(messageClair, phoneNumber, true); // true = via SMS

        smsLoggingService.logSms(phoneNumber, messageClair, payload.getReceivedAt());
        smsResponseService.sendResponse(phoneNumber, reponseAccuse);

        return smsResponseService.createSuccessResponse();
    }

    // ==================== NOUVEAUX ENDPOINTS INTERNET ====================
    
    /**
     * Endpoint pour login via internet (avec ou sans réponse SMS)
     */
    @PostMapping("/login")
    public LoginResponse handleLogin(@RequestBody LoginRequest request) {
        try {
            // Vérifier si le numéro est autorisé
            if (!allowedNumbersService.isNumeroAutorise(request.getPhoneNumber())) {
                return new LoginResponse(false, "Numéro non autorisé", null);
            }

            // Déchiffrer le message de login
            String messageClair = smsProcessingService.processMessage(request.getEncryptedMessage(), request.getPhoneNumber());
            
            // Vérifier que c'est bien un message de login
            SmsProcessingService.TypeMessage typeMessage = smsProcessingService.determineMessageType(messageClair);
            if (typeMessage != SmsProcessingService.TypeMessage.LOGIN) {
                return new LoginResponse(false, "Message n'est pas un login valide", null);
            }

            // Traiter le login
            boolean loginValide = smsProcessingService.processLogin(messageClair);
            String userId = loginValide ? smsProcessingService.getUserId(messageClair) : null;
            
            String message = loginValide ? 
                "Connexion réussie! ID: " + userId : 
                "Échec connexion: Identifiants invalides";

            // Si demandé, envoyer aussi par SMS
            if (request.isSendSmsResponse()) {
                smsResponseService.sendResponse(request.getPhoneNumber(), message);
            }

            // Log de l'activité
            smsLoggingService.logSms(request.getPhoneNumber(), messageClair, java.time.LocalDateTime.now().toString());

            return new LoginResponse(loginValide, message, userId);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du login: " + e.getMessage());
            return new LoginResponse(false, "Erreur serveur: " + e.getMessage(), null);
        }
    }

    /**
     * Endpoint pour envoyer des alertes via internet
     */
    @PostMapping("/message-alerte")
    public AlerteResponse handleAlerte(@RequestBody AlerteRequest request) {
        try {
            // Vérifier si le numéro est autorisé
            if (!allowedNumbersService.isNumeroAutorise(request.getPhoneNumber())) {
                return new AlerteResponse(false, "Numéro non autorisé", null);
            }

            // Déchiffrer le message d'alerte
            String messageClair = smsProcessingService.processMessage(request.getEncryptedMessage(), request.getPhoneNumber());
            
            // Vérifier que c'est bien un message d'alerte
            SmsProcessingService.TypeMessage typeMessage = smsProcessingService.determineMessageType(messageClair);
            if (typeMessage != SmsProcessingService.TypeMessage.ALERTE) {
                return new AlerteResponse(false, "Message n'est pas une alerte valide", null);
            }

            // Déterminer le site
            Integer idSite = siteService.determinerIdSite(request.getPhoneNumber());
            if (idSite == null) {
                return new AlerteResponse(false, "Impossible de déterminer le site", null);
            }

            // Traiter l'alerte
            String reponseAccuse = alerteService.processAlerte(messageClair, idSite, request.getPhoneNumber());

            // Si demandé, envoyer aussi par SMS
            if (request.isSendSmsResponse()) {
                smsResponseService.sendResponse(request.getPhoneNumber(), reponseAccuse);
            }

            // Log de l'activité
            smsLoggingService.logSms(request.getPhoneNumber(), messageClair, java.time.LocalDateTime.now().toString());

            return new AlerteResponse(true, reponseAccuse, idSite);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de l'alerte: " + e.getMessage());
            return new AlerteResponse(false, "Erreur serveur: " + e.getMessage(), null);
        }
    }

    /**
     * Endpoint pour mettre à jour le statut d'un message
     */
    @PostMapping("/update-status")
    public StatusUpdateResponse handleStatusUpdate(@RequestBody StatusUpdateRequest request) {
        try {
            // Vérifier si le numéro est autorisé
            if (!allowedNumbersService.isNumeroAutorise(request.getPhoneNumber())) {
                return new StatusUpdateResponse(false, "Numéro non autorisé");
            }

            // Déchiffrer le message de mise à jour de statut
            String messageClair = smsProcessingService.processMessage(request.getEncryptedMessage(), request.getPhoneNumber());
            
            System.out.println("🔄 Message de mise à jour statut déchiffré: " + messageClair);

            SmsProcessingService.TypeMessage typeMessage = smsProcessingService.determineMessageType(messageClair);
            if (typeMessage != SmsProcessingService.TypeMessage.STATUS_UPDATE) {
                return new StatusUpdateResponse(false,
                    "Format invalide pour mise à jour statut. Format attendu: dateChangement/idMessage/idStatus ou date/id/id");
            }

            String[] statusParts = extractStatusFields(messageClair);
            if (statusParts == null) {
                return new StatusUpdateResponse(false,
                    "Champs requis manquants pour la mise à jour statut");
            }

            // Traiter la mise à jour de statut
            String resultat = historiqueMessageStatusService.updateMessageStatus(
                statusParts[0],
                statusParts[1],
                statusParts[2]
            );

            // Si demandé, envoyer aussi par SMS
            if (request.isSendSmsResponse()) {
                smsResponseService.sendResponse(request.getPhoneNumber(), resultat);
            }

            // Log de l'activité
            smsLoggingService.logSms(request.getPhoneNumber(), messageClair, java.time.LocalDateTime.now().toString());

            boolean success = resultat.startsWith("✅");
            return new StatusUpdateResponse(success, resultat);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            return new StatusUpdateResponse(false, "Erreur serveur: " + e.getMessage());
        }
    }
    
    // ==================== ENDPOINT UNIVERSEL POUR TOUS LES MESSAGES ====================
@PostMapping("/message")
public ResponseEntity<?> handleAnyMessage(@RequestBody Map<String, Object> request) {
    try {
        System.out.println("📨 Message reçu: " + request);
        
        // Format 1: Via webhook (event + payload)
        if (request.containsKey("event") && request.containsKey("payload")) {
            String event = (String) request.get("event");
            if ("sms:received".equals(event)) {
                Map<String, Object> payload = (Map<String, Object>) request.get("payload");
                String phoneNumber = (String) payload.get("phoneNumber");
                String message = (String) payload.get("message");
                String receivedAt = (String) payload.get("receivedAt");
                
                return handleWebhookFormat(phoneNumber, message, receivedAt);
            }
        }
        
        // Format 2: Direct (phoneNumber + message)
        else if (request.containsKey("phoneNumber") && request.containsKey("message")) {
            String phoneNumber = (String) request.get("phoneNumber");
            String message = (String) request.get("message");
            
            return handleDirectFormat(phoneNumber, message);
        }
        
        // Format non reconnu
        return ResponseEntity.badRequest().body("Format de message non supporté");
        
    } catch (Exception e) {
        System.err.println("❌ Erreur traitement message: " + e.getMessage());
        return ResponseEntity.status(500).body("Erreur serveur");
    }
}

private ResponseEntity<?> handleWebhookFormat(String phoneNumber, String message, String receivedAt) {
    try {
        String messageClair = smsProcessingService.processMessage(message, phoneNumber);
        String reponseAccuse = traiterMessage(messageClair, phoneNumber, true);
        
        smsLoggingService.logSms(phoneNumber, messageClair, receivedAt);
        smsResponseService.sendResponse(phoneNumber, reponseAccuse);
        
        return ResponseEntity.ok().body(Map.of("status", "success", "message", reponseAccuse));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
    }
}

private ResponseEntity<?> handleDirectFormat(String phoneNumber, String message) {
    try {
        String messageClair = smsProcessingService.processMessage(message, phoneNumber);
        String reponseAccuse = traiterMessage(messageClair, phoneNumber, true);
        
        smsLoggingService.logSms(phoneNumber, messageClair, java.time.LocalDateTime.now().toString());
        smsResponseService.sendResponse(phoneNumber, reponseAccuse);
        
        return ResponseEntity.ok().body(Map.of("status", "success", "message", reponseAccuse));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
    }
}

    // ==================== MÉTHODE COMMUNE DE TRAITEMENT ====================
    
    /**
     * Méthode commune pour traiter tous les types de messages
     */
    private String traiterMessage(String messageClair, String phoneNumber, boolean viaSms) {
        SmsProcessingService.TypeMessage typeMessage = smsProcessingService.determineMessageType(messageClair);
        System.out.println("📨 Type de message détecté: " + typeMessage);

        String reponseAccuse = null;
        
        switch (typeMessage) {
            case MESSAGE_SIMPLE:
                // reponseAccuse = null;
                break;

            case STATUS_UPDATE:
                String[] statusParts = extractStatusFields(messageClair);
                if (statusParts == null) {
                    reponseAccuse = "❌ Champs requis manquants pour la mise à jour statut";
                    break;
                }
                System.out.println("🔄 Mise à jour de statut détectée");
                reponseAccuse = historiqueMessageStatusService.updateMessageStatus(
                    statusParts[0],
                    statusParts[1],
                    statusParts[2]
                );
                break;
                
            case LOGIN:
                System.out.println("🔐 Tentative de login - Vérification credentials");
                boolean loginValide = smsProcessingService.processLogin(messageClair);
                reponseAccuse = loginValide ? 
                    "Connexion réussie! ID: " + smsProcessingService.getUserId(messageClair) : 
                    "Échec connexion: Identifiants invalides";
                break;
                
            case ALERTE:
                System.out.println("🚨 Alerte détectée - Traitement spécialisé");
                Integer idSite = siteService.determinerIdSite(phoneNumber);
                
                if (idSite != null) {
                    reponseAccuse = alerteService.processAlerte(messageClair, idSite, phoneNumber);
                } else {
                    reponseAccuse = "❌ Impossible de déterminer le site";
                }
                break;

            // default:
            //     // Vérifier si c'est une mise à jour de statut (3 séparateurs)
            //     String[] parties = messageClair.split("/", -1);
            //     if (parties.length == 3) {
            //         System.out.println("🔄 Mise à jour de statut détectée");
            //         reponseAccuse = historiqueMessageStatusService.updateMessageStatus(
            //             parties[0], parties[1], parties[2]);
            //     }
            //     break;
        }

        return reponseAccuse;
    }

    private String[] extractStatusFields(String messageClair) {
        Map<String, String> v2Fields = smsProcessingService.parseV2Fields(messageClair);
        if (!v2Fields.isEmpty()) {
            String dateChangement = valueOrEmpty(v2Fields.get("dateChangement"));
            String idMessage = valueOrEmpty(v2Fields.get("idMessage"));
            String idStatus = valueOrEmpty(v2Fields.get("idStatus"));
            if (dateChangement.isEmpty() || idMessage.isEmpty() || idStatus.isEmpty()) {
                return null;
            }
            return new String[] { dateChangement, idMessage, idStatus };
        }

        String[] legacyParts = messageClair.trim().split("/", -1);
        if (legacyParts.length != 3) {
            return null;
        }
        return new String[] {
            legacyParts[0].trim(),
            legacyParts[1].trim(),
            legacyParts[2].trim()
        };
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    // ==================== CLASSES DE REQUÊTE ET RÉPONSE ====================
    
    // Classes pour les requêtes
    public static class LoginRequest {
        private String phoneNumber;
        private String encryptedMessage;
        private boolean sendSmsResponse = false;
        
        // Getters et setters
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEncryptedMessage() { return encryptedMessage; }
        public void setEncryptedMessage(String encryptedMessage) { this.encryptedMessage = encryptedMessage; }
        public boolean isSendSmsResponse() { return sendSmsResponse; }
        public void setSendSmsResponse(boolean sendSmsResponse) { this.sendSmsResponse = sendSmsResponse; }
    }

    public static class AlerteRequest {
        private String phoneNumber;
        private String encryptedMessage;
        private boolean sendSmsResponse = false;
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEncryptedMessage() { return encryptedMessage; }
        public void setEncryptedMessage(String encryptedMessage) { this.encryptedMessage = encryptedMessage; }
        public boolean isSendSmsResponse() { return sendSmsResponse; }
        public void setSendSmsResponse(boolean sendSmsResponse) { this.sendSmsResponse = sendSmsResponse; }

        // toString pour le debugging
        @Override
        public String toString() {
            return "AlerteRequest{phoneNumber='" + phoneNumber + "', encryptedMessage='" + encryptedMessage + "', sendSmsResponse=" + sendSmsResponse + "}";
        }
    }

    public static class StatusUpdateRequest {
        private String phoneNumber;
        private String encryptedMessage;
        private boolean sendSmsResponse = false;
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEncryptedMessage() { return encryptedMessage; }
        public void setEncryptedMessage(String encryptedMessage) { this.encryptedMessage = encryptedMessage; }
        public boolean isSendSmsResponse() { return sendSmsResponse; }
        public void setSendSmsResponse(boolean sendSmsResponse) { this.sendSmsResponse = sendSmsResponse; }
    }

    // Classes pour les réponses
    public static class LoginResponse {
        private boolean success;
        private String message;
        private String userId;
        
        public LoginResponse(boolean success, String message, String userId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUserId() { return userId; }
    }

    public static class AlerteResponse {
        private boolean success;
        private String message;
        private Integer siteId;
        
        public AlerteResponse(boolean success, String message, Integer siteId) {
            this.success = success;
            this.message = message;
            this.siteId = siteId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getSiteId() { return siteId; }
    }

    public static class StatusUpdateResponse {
        private boolean success;
        private String message;
        
        public StatusUpdateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // ==================== CLASSES WEBHOOK EXISTANTES ====================
    
    public static class GatewayWebhookRequest {
        private String event;
        private Payload payload;
        
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public Payload getPayload() { return payload; }
        public void setPayload(Payload payload) { this.payload = payload; }

        public static class Payload {
            private String phoneNumber;
            private String message;
            private String receivedAt;
            
            public String getPhoneNumber() { return phoneNumber; }
            public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
            public String getMessage() { return message; }
            public void setMessage(String message) { this.message = message; }
            public String getReceivedAt() { return receivedAt; }
            public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }
        }
    }

}