package com.example.serveur.service;

import com.example.serveur.model.Alerte;
import com.example.serveur.model.TypeAlerte;
import com.example.serveur.model.Site;
import com.example.serveur.model.Message;
import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.repository.AlerteRepository;
import com.example.serveur.repository.TypeAlerteRepository;
import com.example.serveur.repository.StatusMessageRepository;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.SiteRepository;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NiveauAlerteService {
    
    private final TypeAlerteRepository typeAlerteRepository;
    private final AlerteRepository alerteRepository;
    private final StatusMessageRepository statusMessageRepository;
    private final InterventionRepository interventionRepository;
    private final SiteRepository siteRepository;
    private final MessageRepository messageRepository;
    private final HistoriqueMessageStatusRepository historiqueMessageStatusRepository;
    
    public NiveauAlerteService(TypeAlerteRepository typeAlerteRepository,
                              AlerteRepository alerteRepository,
                              StatusMessageRepository statusMessageRepository,
                              InterventionRepository interventionRepository,
                              SiteRepository siteRepository,
                              MessageRepository messageRepository,
                              HistoriqueMessageStatusRepository historiqueMessageStatusRepository) {
        this.typeAlerteRepository = typeAlerteRepository;
        this.alerteRepository = alerteRepository;
        this.statusMessageRepository = statusMessageRepository;     
        this.interventionRepository = interventionRepository;
        this.siteRepository = siteRepository;
        this.messageRepository = messageRepository;
        this.historiqueMessageStatusRepository = historiqueMessageStatusRepository;
    }

    public String determinerNiveauAlerteParId(Integer idStatus, Integer idIntervention, Boolean renfort) {
        if (idStatus == null || idIntervention == null || renfort == null) {
            return "Inconnu";
        }
        
        // LOGIQUE REVISÉE - Ordre croissant de gravité
        if (idStatus == 3) { // Maîtrisé - Situation sous contrôle
            return "Vert";
        }
        else if (idStatus == 1) { // Début de feu - Situation naissante
            if (idIntervention == 1) { // Possible - Intervention réalisable
                return "Vert";
            } else if (idIntervention == 2) { // Partielle - Intervention partielle
                return "Jaune";
            } else if (idIntervention == 3) { // Impossible - Intervention impossible
                return renfort ? "Orange" : "Rouge"; // Avec renfort: Orange, Sans: Rouge
            }
        }
        else if (idStatus == 2) { // En cours - Situation évolutive
            if (idIntervention == 1) { // Possible 
                return "Jaune";
            } else if (idIntervention == 2) { // Partielle
                return renfort ? "Orange" : "Rouge"; // Avec renfort: Orange, Sans: Rouge
            } else if (idIntervention == 3) { // Impossible
                return "Rouge"; // Situation critique
            }
        }
        
        return "Inconnu";
    }
    
    /**
     * Crée une alerte dans la base de données
     */
    public void creerAlerte(Integer idSite, Integer idMessage, String niveauAlerte) {
        try {
            // Trouver le type d'alerte correspondant
            TypeAlerte typeAlerte = typeAlerteRepository.findByZone(niveauAlerte);
            
            if (typeAlerte != null) {
                // Vérifier que le site existe
                Optional<Site> siteOpt = siteRepository.findById(idSite);
                if (siteOpt.isEmpty()) {
                    System.err.println("❌ Site non trouvé avec ID: " + idSite);
                    return;
                }
                
                // Vérifier que le message existe
                Optional<Message> messageOpt = messageRepository.findById(idMessage);
                if (messageOpt.isEmpty()) {
                    System.err.println("❌ Message non trouvé avec ID: " + idMessage);
                    return;
                }
                
                // Mettre à jour la dernière alerte de ce message si elle existe, sinon en créer une
                Alerte alerte = alerteRepository.findTopByMessage_IdMessageOrderByIdAlerteDesc(idMessage)
                    .orElseGet(Alerte::new);
                alerte.setSite(siteOpt.get());
                alerte.setMessage(messageOpt.get());
                alerte.setTypeAlerte(typeAlerte);

                alerteRepository.save(alerte);
                
                System.out.println("✅ Alerte créée: Site=" + idSite + 
                                  ", Message=" + idMessage + 
                                  ", Niveau=" + niveauAlerte);
            } else {
                System.err.println("❌ Type d'alerte non trouvé: " + niveauAlerte);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'alerte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recalcule et persiste le niveau d'alerte à partir de l'état actuel d'un message.
     */
    public String recalculerEtPersisterAlerteMessage(Message message) {
        if (message == null || message.getIdMessage() <= 0 || message.getIntervention() == null) {
            return "Inconnu";
        }

        if (message.getUserApp() == null || message.getUserApp().getPatrouilleur() == null || message.getUserApp().getPatrouilleur().getSite() == null) {
            return "Inconnu";
        }

        Optional<HistoriqueMessageStatus> lastStatus = historiqueMessageStatusRepository
                .findTopByMessage_IdMessageOrderByDateChangementDesc(message.getIdMessage());
        if (lastStatus.isEmpty() || lastStatus.get().getStatus() == null) {
            return "Inconnu";
        }

        String status = lastStatus.get().getStatus().getStatus();
        String intervention = message.getIntervention().getIntervention();
        Boolean renfort = message.getRenfort() != null ? message.getRenfort() : false;
        Integer idSite = message.getUserApp().getPatrouilleur().getSite().getId_Site();

        return traiterAlerteComplete(status, intervention, renfort, idSite, message.getIdMessage());
    }
    
    /**
     * Méthode complète pour traiter une alerte et déterminer son niveau
     */
    public String traiterAlerteComplete(String status, String intervention, Boolean renfort, 
                                      Integer idSite, Integer idMessage) {

        int idStatus = statusMessageRepository.findByStatus(status).getIdStatusMessage();
        int idIntervention = interventionRepository.findByType(intervention).getId_Intervention();
        String niveau = determinerNiveauAlerteParId(idStatus, idIntervention, renfort);
        
        if (!"Inconnu".equals(niveau)) {
            creerAlerte(idSite, idMessage, niveau);  
        }
        
        return niveau;
    }
}