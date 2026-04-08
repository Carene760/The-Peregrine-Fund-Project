package com.example.serveur.service;

import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.model.Message;
import com.example.serveur.model.StatusMessage;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.repository.StatusMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class HistoriqueMessageStatusService {
    
    private final HistoriqueMessageStatusRepository historiqueMessageStatusRepository;
    private final MessageRepository messageRepository;
    private final StatusMessageRepository statusMessageRepository;
    
    public HistoriqueMessageStatusService(HistoriqueMessageStatusRepository historiqueMessageStatusRepository,
                                  MessageRepository messageRepository,
                                  StatusMessageRepository statusMessageRepository) {
        this.historiqueMessageStatusRepository = historiqueMessageStatusRepository;
        this.messageRepository = messageRepository;
        this.statusMessageRepository = statusMessageRepository;
    }

    public HistoriqueMessageStatus save(HistoriqueMessageStatus historiqueMessageStatus) {
        return historiqueMessageStatusRepository.save(historiqueMessageStatus);
    }

    public List<HistoriqueMessageStatus> findAll() {
        return historiqueMessageStatusRepository.findAll();
    }

    public Optional<HistoriqueMessageStatus> findById(int id) {
        return historiqueMessageStatusRepository.findById(id);
    }

    public void deleteById(int id) {
        historiqueMessageStatusRepository.deleteById(id);
    }
    
    /**
     * Met à jour le statut d'un message et crée un historique
     * Format attendu: date_changement/id_message/id_status
     */
    @Transactional
    public String updateMessageStatus(String dateChangementStr, String idMessageStr, String idStatusStr) {
        try {
            System.out.println("🔄 Traitement mise à jour statut:");
            System.out.println("  - Date: '" + dateChangementStr + "'");
            System.out.println("  - ID Message: '" + idMessageStr + "'");
            System.out.println("  - ID Status: '" + idStatusStr + "'");
            
            // Parser et valider les données
            LocalDateTime dateChangement = parseDateTime(dateChangementStr);
            Integer idMessage = parseInt(idMessageStr);
            Integer idStatus = parseInt(idStatusStr);
            
            // Validation des champs obligatoires
            if (dateChangement == null) {
                return "❌ Date de changement manquante ou invalide: '" + dateChangementStr + "'";
            }
            
            if (idMessage == null) {
                return "❌ ID Message manquant ou invalide: '" + idMessageStr + "'";
            }
            
            if (idStatus == null) {
                return "❌ ID Status manquant ou invalide: '" + idStatusStr + "'";
            }
            
            // Vérifier que le message existe
            Optional<Message> messageOpt = messageRepository.findById(idMessage);
            if (messageOpt.isEmpty()) {
                return "❌ Message non trouvé avec ID: " + idMessage;
            }
            
            // Vérifier que le statut existe
            Optional<StatusMessage> statusOpt = statusMessageRepository.findById(idStatus);
            if (statusOpt.isEmpty()) {
                return "❌ Statut non trouvé avec ID: " + idStatus;
            }
            
            // Vérifier s'il y a déjà un historique récent avec le même statut pour ce message
            if (historiqueMessageStatusRepository.existsByMessageAndIdStatusAndDateChangementAfter(
                    messageOpt.get(), statusOpt.get(), dateChangement.minusMinutes(1))) {
                return "⚠️ Mise à jour de statut ignorée - Changement récent déjà enregistré";
            }
            
            // Créer le nouvel historique de statut
            HistoriqueMessageStatus nouvelHistorique = new HistoriqueMessageStatus();
            nouvelHistorique.setDateChangement(dateChangement);
            nouvelHistorique.setMessage(messageOpt.get());
            nouvelHistorique.setIdStatus(statusOpt.get());
            
            // Sauvegarder
            HistoriqueMessageStatus saved = historiqueMessageStatusRepository.save(nouvelHistorique);
            
            String statusText = statusOpt.get().getStatus();
            
            System.out.println("✅ Historique de statut créé:");
            System.out.println("  - ID Historique: " + saved.getIdHistorique());
            System.out.println("  - Message ID: " + idMessage);
            System.out.println("  - Nouveau statut: " + statusText);
            System.out.println("  - Date: " + dateChangement);
            
            return String.format("✅ Statut du message %d mis à jour vers '%s' le %s", 
                                idMessage, statusText, dateChangement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();
            return "❌ Erreur lors de la mise à jour du statut: " + e.getMessage();
        }
    }
    
    /**
     * Récupère le dernier statut d'un message
     */
    public Optional<HistoriqueMessageStatus> getDernierStatut(Integer idMessage) {
        return historiqueMessageStatusRepository.findTopByMessage_IdMessageOrderByDateChangementDesc(idMessage);
    }
    
    /**
     * Récupère tous les changements de statut pour un message
     */
    public java.util.List<HistoriqueMessageStatus> getHistoriqueMessage(Integer idMessage) {
        return historiqueMessageStatusRepository.findByMessage_IdMessageOrderByDateChangementDesc(idMessage);
    }
    
    // Méthodes utilitaires pour le parsing (similaires à AlerteService)
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (value.length() <= 10) {
                        // Si c'est juste une date, ajouter l'heure courante
                        return LocalDateTime.parse(value + "T" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), 
                                                 DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } else {
                        return LocalDateTime.parse(value, formatter);
                    }
                } catch (DateTimeParseException e) {
                    // Continuer avec le formateur suivant
                }
            }
            
            // Si aucun formateur ne fonctionne, essayer avec la date courante
            System.out.println("⚠️ Format de date non reconnu, utilisation de la date courante");
            return LocalDateTime.now();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing date: " + e.getMessage());
            return LocalDateTime.now(); // Fallback sur la date courante
        }
    }
    
    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("❌ Impossible de convertir en Integer : '" + value + "'");
            return null;
        }
    }
}