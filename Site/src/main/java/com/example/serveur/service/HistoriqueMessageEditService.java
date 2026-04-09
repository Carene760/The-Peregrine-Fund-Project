package com.example.serveur.service;

import com.example.serveur.model.Message;
import com.example.serveur.model.User;
import com.example.serveur.model.Intervention;
import com.example.serveur.model.Evenement;
import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.model.Alerte;
import com.example.serveur.model.TypeAlerte;
import com.example.serveur.model.StatusMessage;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.EvenementRepository;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import com.example.serveur.repository.StatusMessageRepository;
import com.example.serveur.repository.AlerteRepository;
import com.example.serveur.repository.TypeAlerteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class HistoriqueMessageEditService {

    private final MessageService messageService;
    private final InterventionRepository interventionRepository;
    private final EvenementRepository evenementRepository;
    private final HistoriqueMessageStatusRepository historiqueMessageStatusRepository;
    private final StatusMessageRepository statusMessageRepository;
    private final AlerteRepository alerteRepository;
    private final TypeAlerteRepository typeAlerteRepository;
    private final NiveauAlerteService niveauAlerteService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public HistoriqueMessageEditService(MessageService messageService, 
                                       InterventionRepository interventionRepository,
                                       EvenementRepository evenementRepository,
                                       HistoriqueMessageStatusRepository historiqueMessageStatusRepository,
                                       StatusMessageRepository statusMessageRepository,
                                       AlerteRepository alerteRepository,
                                       TypeAlerteRepository typeAlerteRepository,
                                       NiveauAlerteService niveauAlerteService) {
        this.messageService = messageService;
        this.interventionRepository = interventionRepository;
        this.evenementRepository = evenementRepository;
        this.historiqueMessageStatusRepository = historiqueMessageStatusRepository;
        this.statusMessageRepository = statusMessageRepository;
        this.alerteRepository = alerteRepository;
        this.typeAlerteRepository = typeAlerteRepository;
        this.niveauAlerteService = niveauAlerteService;
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

        StatusMessage newStatus = null;
        boolean alertManuallyEdited = Boolean.parseBoolean(String.valueOf(updateData.getOrDefault("alertManuallyEdited", "false")));

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

        // idStatus (historique message status)
        if (updateData.containsKey("idStatus") && updateData.get("idStatus") != null) {
            try {
                Integer statusId = Integer.parseInt(String.valueOf(updateData.get("idStatus")));
                newStatus = statusMessageRepository.findById(statusId).orElse(null);
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        // idTypeAlerte
        if (alertManuallyEdited && updateData.containsKey("idTypeAlerte") && updateData.get("idTypeAlerte") != null) {
            try {
                Integer typeAlerteId = Integer.parseInt(String.valueOf(updateData.get("idTypeAlerte")));
                TypeAlerte typeAlerte = typeAlerteRepository.findById(typeAlerteId).orElse(null);
                if (typeAlerte != null) {
                    Alerte alerte = alerteRepository.findTopByMessage_IdMessageOrderByIdAlerteDesc(message.getIdMessage())
                            .orElseGet(Alerte::new);
                    alerte.setMessage(message);
                    alerte.setTypeAlerte(typeAlerte);
                    if (message.getUserApp() != null && message.getUserApp().getPatrouilleur() != null && message.getUserApp().getPatrouilleur().getSite() != null) {
                        alerte.setSite(message.getUserApp().getPatrouilleur().getSite());
                    }
                    alerteRepository.save(alerte);
                }
            } catch (Exception e) {
                // Ignorer si conversion échoue
            }
        }

        Message savedMessage = messageService.saveMessage(message);

        if (newStatus != null) {
            HistoriqueMessageStatus historique = new HistoriqueMessageStatus();
            historique.setMessage(savedMessage);
            historique.setDateChangement(LocalDateTime.now());
            historique.setIdStatus(newStatus);
            historiqueMessageStatusRepository.save(historique);
        }

        if (!alertManuallyEdited) {
            niveauAlerteService.recalculerEtPersisterAlerteMessage(savedMessage);
        }
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
