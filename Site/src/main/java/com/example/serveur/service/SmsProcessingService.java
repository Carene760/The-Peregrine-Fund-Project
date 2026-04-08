package com.example.serveur.service;

import com.example.serveur.model.UserApp;
import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
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
        int nbSeparateurs = compterOccurrences(message, separateur);
        return determinerTypeMessage(nbSeparateurs);
    }

    public boolean processLogin(String message) {
        try {
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
        } else if (nbSeparateurs == 1) {
            return TypeMessage.LOGIN;
        } else {
            return TypeMessage.MESSAGE_SIMPLE;
        }
    }

    public enum TypeMessage {
        MESSAGE_SIMPLE,
        LOGIN,
        ALERTE
    }
}