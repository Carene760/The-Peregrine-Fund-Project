package com.example.serveur.service;

import com.example.serveur.model.Message;
import com.example.serveur.model.User;
import com.example.serveur.model.Intervention;
import com.example.serveur.model.Evenement;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.EvenementRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class HistoriqueMessageEditService {

    private final MessageService messageService;
    private final InterventionRepository interventionRepository;
    private final EvenementRepository evenementRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public HistoriqueMessageEditService(MessageService messageService, 
                                       InterventionRepository interventionRepository,
                                       EvenementRepository evenementRepository) {
        this.messageService = messageService;
        this.interventionRepository = interventionRepository;
        this.evenementRepository = evenementRepository;
    }

    /**
     * Met à jour tous les champs modifiables d'un message si l'utilisateur est ADMIN
     */
    public boolean updateMessageAsAdmin(User currentUser, int messageId, Map<String, Object> updateData) {
        if (!isAdmin(currentUser)) {
            return false;
        }

        Message message = messageService.getMessageById(messageId);
        if (message == null) {
            return false;
        }

        // dateCommencement
        if (updateData.containsKey("dateCommencement") && updateData.get("dateCommencement") != null) {
            LocalDateTime dt = parseLocalDateTime((String) updateData.get("dateCommencement"));
            if (dt != null) message.setDateCommencement(dt);
        }

        // dateSignalement
        if (updateData.containsKey("dateSignalement") && updateData.get("dateSignalement") != null) {
            LocalDateTime dt = parseLocalDateTime((String) updateData.get("dateSignalement"));
            if (dt != null) message.setDateSignalement(dt);
        }

        // pointRepere
        if (updateData.containsKey("pointRepere")) {
            message.setPointRepere(normalizeString((String) updateData.get("pointRepere")));
        }

        // surfaceApproximative
        if (updateData.containsKey("surfaceApproximative") && updateData.get("surfaceApproximative") != null) {
            try {
                Double surface = Double.parseDouble(String.valueOf(updateData.get("surfaceApproximative")));
                message.setSurfaceApproximative(surface);
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        // description
        if (updateData.containsKey("description")) {
            message.setDescription(normalizeString((String) updateData.get("description")));
        }

        // direction
        if (updateData.containsKey("direction")) {
            message.setDirection(normalizeString((String) updateData.get("direction")));
        }

        // renfort
        if (updateData.containsKey("renfort") && updateData.get("renfort") != null) {
            String renfortStr = String.valueOf(updateData.get("renfort")).toLowerCase();
            message.setRenfort("true".equals(renfortStr) || "oui".equals(renfortStr) || "1".equals(renfortStr));
        }

        // longitude
        if (updateData.containsKey("longitude") && updateData.get("longitude") != null) {
            try {
                Double longitude = Double.parseDouble(String.valueOf(updateData.get("longitude")));
                message.setLongitude(longitude);
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        // latitude
        if (updateData.containsKey("latitude") && updateData.get("latitude") != null) {
            try {
                Double latitude = Double.parseDouble(String.valueOf(updateData.get("latitude")));
                message.setLatitude(latitude);
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        // idIntervention
        if (updateData.containsKey("idIntervention") && updateData.get("idIntervention") != null) {
            try {
                Integer interventionId = Integer.parseInt(String.valueOf(updateData.get("idIntervention")));
                Intervention intervention = interventionRepository.findById(interventionId).orElse(null);
                if (intervention != null) {
                    message.setIntervention(intervention);
                }
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        // idEvenement
        if (updateData.containsKey("idEvenement") && updateData.get("idEvenement") != null) {
            try {
                Integer evenementId = Integer.parseInt(String.valueOf(updateData.get("idEvenement")));
                if (evenementId > 0) {
                    Evenement evenement = evenementRepository.findById(evenementId).orElse(null);
                    message.setEvenement(evenement);
                } else {
                    message.setEvenement(null);
                }
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        messageService.saveMessage(message);
        return true;
    }

    public boolean isAdmin(User currentUser) {
        return currentUser != null
                && currentUser.getFonction() != null
                && "Administrateur".equalsIgnoreCase(currentUser.getFonction().getFonction());
    }

    private LocalDateTime parseLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeString(String str) {
        return (str != null) ? str.trim() : "";
    }
}
