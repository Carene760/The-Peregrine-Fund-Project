package com.example.serveur.service;

import com.example.serveur.model.UserApp;
import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final String separateur;

    public SmsProcessingService(EncryptionUtil encryptionUtil,
                               UserAppRepository userAppRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${message.separator}") String separateur) {
        this.encryptionUtil = encryptionUtil;
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
        this.separateur = separateur;
    }

    public String processMessage(String messageChiffre, String phoneNumber) throws Exception {
         // NETTOYAGE DU MESSAGE - supprimer les espaces, retours à la ligne, etc.
        messageChiffre = messageChiffre.trim().replace("\n", "").replace("\r", "");
        
        System.out.println("🔍 Message après nettoyage: '" + messageChiffre + "'");
        
        // Vérifier si le message ressemble vraiment à un payload chiffré AES.
        // Certains textes courts comme "OK" sont techniquement décodables en Base64,
        // mais ne peuvent pas être un ciphertext valide pour AES.
        if (!isLikelyEncryptedPayload(messageChiffre)) {
            return messageChiffre; // Déjà en clair
        }
        
        // Sinon déchiffrer normalement
        return encryptionUtil.dechiffrer(messageChiffre);
    }

    // Chiffrement AES/GCM: IV (12 octets) + tag d'authentification (16 octets)
    // = 28 octets de surcoût minimum, quelle que soit la taille du texte
    // clair. Contrairement à l'ancien mode ECB (aligné sur des blocs de 16
    // octets), un ciphertext GCM n'a AUCUNE contrainte d'alignement: ne pas
    // vérifier "% 16 == 0" ici, sous peine de rejeter à tort de vrais
    // messages chiffrés comme "déjà en clair".
    private static final int GCM_MIN_CIPHERTEXT_BYTES = 28;

    private boolean isLikelyEncryptedPayload(String str) {
        try {
            String cleaned = str.replaceAll("\\s+", "").replace("-", "+").replace("_", "/");
            if (cleaned.length() < 16) {
                return false;
            }

            if (cleaned.length() % 4 != 0) {
                cleaned = cleaned + "=".repeat((4 - cleaned.length() % 4) % 4);
            }

            byte[] decoded = Base64.getDecoder().decode(cleaned);
            return decoded.length >= GCM_MIN_CIPHERTEXT_BYTES;
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
                return checkCredentials(login, motDePasse);
            }

            String[] parties = message.split("\\" + separateur);
            if (parties.length != 2) {
                return false;
            }

            String login = parties[0].trim();
            String motDePasse = parties[1].trim();

            return checkCredentials(login, motDePasse);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie les identifiants d'un utilisateur mobile (userapp) en comparant
     * le mot de passe fourni au hash BCrypt stocké en base.
     */
    private boolean checkCredentials(String login, String rawPassword) {
        Optional<UserApp> userOpt = userAppRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            return false;
        }
        String storedHash = userOpt.get().getMotDePasse();
        return storedHash != null && passwordEncoder.matches(rawPassword, storedHash);
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
