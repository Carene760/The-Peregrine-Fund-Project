package com.example.serveur.service;

import com.example.serveur.model.UserApp;
import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SmsProcessingService {
    
    private final EncryptionUtil encryptionUtil;
    private final UserAppRepository userAppRepository;
    private final String separateur;

    public SmsProcessingService(EncryptionUtil encryptionUtil, 
                               UserAppRepository userAppRepository,
                               @Value("${message.separator}") String separateur) {
        this.encryptionUtil = encryptionUtil;
        this.userAppRepository = userAppRepository;
        this.separateur = separateur;
    }

    public String processMessage(String messageChiffre, String phoneNumber) throws Exception {
         // NETTOYAGE DU MESSAGE - supprimer les espaces, retours à la ligne, etc.
        messageChiffre = messageChiffre.trim().replace("\n", "").replace("\r", "");
        
        System.out.println("🔍 Message après nettoyage: '" + messageChiffre + "'");
        
        // Vérifier si le message est déjà en clair
        if (!isBase64(messageChiffre)) {
            return messageChiffre; // Déjà en clair
        }
        
        // Sinon déchiffrer normalement
        return encryptionUtil.dechiffrer(messageChiffre);
    }

    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public TypeMessage determineMessageType(String message) {
        Map<String, String> v2Fields = parseV2Fields(message);
        if (!v2Fields.isEmpty()) {
            System.out.println("🧭 Parser source: V2 key=value");
            if (hasRequiredFields(v2Fields, "login", "password")) {
                return TypeMessage.LOGIN;
            }
            if (hasRequiredFields(v2Fields, "dateChangement", "idMessage", "idStatus")) {
                return TypeMessage.STATUS_UPDATE;
            }
            if (hasRequiredFields(v2Fields, "dateSignalement", "idIntervention", "idUserApp", "idStatus")) {
                return TypeMessage.ALERTE;
            }
            return TypeMessage.MESSAGE_SIMPLE;
        }

        System.out.println("🧭 Parser source: legacy séparateur");
        int nbSeparateurs = compterOccurrences(message, separateur);
        return determinerTypeMessage(nbSeparateurs);
    }

    public boolean processLogin(String message) {
        try {
            Map<String, String> v2Fields = parseV2Fields(message);
            if (!v2Fields.isEmpty()) {
                String login = valueOrEmpty(v2Fields.get("login"));
                String motDePasse = valueOrEmpty(v2Fields.get("password"));
                if (login.isEmpty() || motDePasse.isEmpty()) {
                    return false;
                }
                return userAppRepository.findByLoginAndMotDePasse(login, motDePasse).isPresent();
            }

            String[] parties = message.split("\\" + separateur);
            if (parties.length != 2) {
                return false;
            }
            
            String login = parties[0].trim();
            String motDePasse = parties[1].trim();
            
            Optional<UserApp> userOpt = userAppRepository.findByLoginAndMotDePasse(login, motDePasse);
            return userOpt.isPresent();
            
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserId(String message) {
        try {
            Map<String, String> v2Fields = parseV2Fields(message);
            if (!v2Fields.isEmpty()) {
                String login = valueOrEmpty(v2Fields.get("login"));
                if (login.isEmpty()) {
                    return "inconnu";
                }
                return userAppRepository.findIdByLogin(login)
                        .map(String::valueOf)
                        .orElse("inconnu");
            }

            String[] parties = message.split("\\" + separateur);
            String login = parties[0].trim();
            
            return userAppRepository.findIdByLogin(login)
                        .map(String::valueOf)
                        .orElse("inconnu");

        } catch (Exception e) {
            return "inconnu";
        }
    }

    private int compterOccurrences(String message, String separateur) {
        if (message == null || separateur == null || separateur.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        while ((index = message.indexOf(separateur, index)) != -1) {
            count++;
            index += separateur.length();
        }
        return count;
    }

    private TypeMessage determinerTypeMessage(int nbSeparateurs) {
        if (nbSeparateurs >= 11) {
            return TypeMessage.ALERTE;
        } else if (nbSeparateurs == 2) {
            return TypeMessage.STATUS_UPDATE;
        } else if (nbSeparateurs == 1) {
            return TypeMessage.LOGIN;
        } else {
            return TypeMessage.MESSAGE_SIMPLE;
        }
    }

    public Map<String, String> parseV2Fields(String message) {
        Map<String, String> fields = new HashMap<>();
        if (message == null || message.isBlank() || !message.contains("=")) {
            return fields;
        }

        String[] segments = message.split("/");
        for (String segment : segments) {
            if (segment == null || segment.isBlank() || !segment.contains("=")) {
                continue;
            }

            String[] keyValue = segment.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0] == null ? "" : keyValue[0].trim();
            if (key.isEmpty()) {
                continue;
            }

            String value = keyValue[1] == null ? "" : keyValue[1].trim();
            fields.put(key, decodeValue(value));
        }

        return fields;
    }

    public boolean hasRequiredFields(Map<String, String> fields, String... requiredKeys) {
        for (String key : requiredKeys) {
            if (valueOrEmpty(fields.get(key)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public enum TypeMessage {
        MESSAGE_SIMPLE,
        LOGIN,
        ALERTE,
        STATUS_UPDATE
    }
}
