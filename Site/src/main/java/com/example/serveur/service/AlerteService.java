package com.example.serveur.service;

import com.example.serveur.model.Alerte;
import com.example.serveur.model.FonctionZoneAlerte;
import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.model.Intervention;
import com.example.serveur.model.Message;
import com.example.serveur.model.StatusMessage;
import com.example.serveur.model.UserApp;
import com.example.serveur.model.User;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.StatusMessageRepository;
import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.repository.UserRepository;
import com.example.serveur.repository.AlerteRepository;
import com.example.serveur.repository.FonctionZoneAlerteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;



import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class AlerteService {
    
    private final MessageRepository messageRepository;
    private final HistoriqueMessageStatusRepository historiqueRepository;
    private final InterventionRepository interventionRepository;
    private final StatusMessageRepository statusMessageRepository;
    private final UserAppRepository userAppRepository;
    private final NiveauAlerteService niveauAlerteService;
    private final InfoRetourService infoRetourService;
    private final SmsResponseService smsResponseService;
    private final FonctionZoneAlerteRepository fonctionZoneAlerteRepository;
    private final UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    
    private final AlerteRepository alerteRepository;
    
    public AlerteService(MessageRepository messageRepository,
                        HistoriqueMessageStatusRepository historiqueRepository,
                        InterventionRepository interventionRepository,
                        StatusMessageRepository statusMessageRepository,
                        UserAppRepository userAppRepository,
                        NiveauAlerteService niveauAlerteService,
                        InfoRetourService infoRetourService,
                        SmsResponseService smsResponseService,
                        AlerteRepository alerteRepository,
                        FonctionZoneAlerteRepository fonctionZoneAlerteRepository, // ← Nouveau
                        UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.historiqueRepository = historiqueRepository;
        this.interventionRepository = interventionRepository;
        this.statusMessageRepository = statusMessageRepository;
        this.userAppRepository = userAppRepository;
        this.niveauAlerteService = niveauAlerteService;
        this.infoRetourService = infoRetourService;
        this.smsResponseService = smsResponseService;
        this.alerteRepository = alerteRepository;
        this.fonctionZoneAlerteRepository = fonctionZoneAlerteRepository;
        this.userRepository = userRepository;
    }

    public Alerte save(Alerte alerte) {
        return alerteRepository.save(alerte);
    }

    public List<Alerte> findAll() {
        return alerteRepository.findAll();
    }

    public Optional<Alerte> findById(int id) {
        return alerteRepository.findById(id);
    }

    public void deleteById(int id) {
        alerteRepository.deleteById(id);
    }
    
    /**
     * Traite un message d'alerte et l'insère en base de données
     */
    @Transactional
    public String processAlerte(String messageAlerte, Integer idSite, String phoneNumber) {
        try {
            boolean isV2Format = messageAlerte != null && messageAlerte.contains("=");

            LocalDateTime dateCommencement;
            LocalDateTime dateSignalement;
            Integer idIntervention;
            Boolean renfort;
            String direction;
            Double surfaceApproximative;
            String pointRepere;
            String description;
            Integer idUserApp;
            Double longitude;
            Double latitude;
            Integer idStatus;
            LocalDateTime dateEnvoi;

            if (isV2Format) {
                System.out.println("🧭 Parser source: V2 key=value");
                Map<String, String> fields = parseKeyValueMessage(messageAlerte);

                dateSignalement = parseDateTime(fields.get("dateSignalement"));
                dateCommencement = parseDateTime(fields.get("dateCommencement"));
                if (dateCommencement == null) {
                    dateCommencement = dateSignalement != null ? dateSignalement : LocalDateTime.now();
                }

                idIntervention = parseInt(fields.get("idIntervention"));
                renfort = parseBoolean(fields.get("renfort"));
                direction = emptyToNull(fields.get("direction"));
                surfaceApproximative = parseDouble(fields.get("surfaceApproximative"));
                pointRepere = emptyToNull(fields.get("pointRepere"));
                description = emptyToNull(fields.get("description"));
                idUserApp = parseInt(fields.get("idUserApp"));
                longitude = parseDouble(fields.get("longitude"));
                latitude = parseDouble(fields.get("latitude"));
                idStatus = parseInt(fields.get("idStatus"));
                dateEnvoi = parseDateTime(fields.get("dateEnvoi"));
            } else {
                System.out.println("🧭 Parser source: legacy séparateur");
                String[] parties = messageAlerte.split("/", -1);

                if (parties.length != 12) {
                    return "❌ Format invalide: " + (parties.length - 1) +
                           " séparateurs trouvés (12 attendus). Éléments manquants.";
                }

                // Debug: Afficher toutes les parties
                System.out.println("🔍 Parties du message:");
                for (int i = 0; i < parties.length; i++) {
                    System.out.println("  [" + i + "]: '" + parties[i] + "'");
                }

                dateCommencement = parseDateTime(parties[0]);
                dateSignalement = parseDateTime(parties[1]);
                idIntervention = parseInt(parties[2]);
                renfort = parseBoolean(parties[3]);
                direction = emptyToNull(parties[4]);
                surfaceApproximative = parseDouble(parties[5]);
                pointRepere = emptyToNull(parties[6]);
                description = emptyToNull(parties[7]);
                idUserApp = parseInt(parties[8]);
                longitude = parseDouble(parties[9]);
                latitude = parseDouble(parties[10]);
                idStatus = parseInt(parties[11]);
                dateEnvoi = null;
            }

            if (dateEnvoi == null) {
                dateEnvoi = LocalDateTime.now();
            }
            
            // Validation renforcée des champs obligatoires
            if (dateCommencement == null) {
                return "❌ Date de commencement manquante ou invalide";
            }
            if (dateSignalement == null) {
                return "❌ Date de signalement manquante ou invalide";
            }
            if (idIntervention == null) {
                return "❌ ID intervention manquant ou invalide";
            }
            if (idUserApp == null) {
                return "❌ ID UserApp manquant ou invalide";
            }
            if (idStatus == null) {
                return "❌ ID Status manquant ou invalide";
            }
            
            System.out.println("✅ Données parsées - ID UserApp: " + idUserApp);

            if (isDuplicateMessage(longitude, latitude)) {
                return "⚠️ Message déjà existant - Doublon ignoré";
            }
            
            // Vérifier que les entités référencées existent
            Optional<Intervention> interventionOpt = interventionRepository.findById(idIntervention);
            if (interventionOpt.isEmpty()) {
                return "❌ Intervention non trouvée avec ID: " + idIntervention;
            }
            
            Optional<UserApp> userAppOpt = userAppRepository.findById(idUserApp);
            if (userAppOpt.isEmpty()) {
                return "❌ UserApp non trouvé avec ID: " + idUserApp;
            }
            
            Optional<StatusMessage> statusOpt = statusMessageRepository.findById(idStatus);
            if (statusOpt.isEmpty()) {
                return "❌ Status non trouvé avec ID: " + idStatus;
            }
            
            // Créer et sauvegarder le message
            Message message = new Message();
            message.setDateCommencement(dateCommencement);
            message.setDateSignalement(dateSignalement);
            message.setDateEnvoi(dateEnvoi);
            message.setIntervention(interventionOpt.get());
            message.setRenfort(renfort);
            message.setDirection(direction);
            message.setSurfaceApproximative(surfaceApproximative);
            message.setPointRepere(pointRepere);
            message.setDescription(description);
            message.setUserApp(userAppOpt.get());
            message.setLongitude(longitude);
            message.setLatitude(latitude);
            
            Message savedMessage = messageRepository.save(message);
            
            // Créer l'historique de statut
            HistoriqueMessageStatus historique = new HistoriqueMessageStatus();
            historique.setDateChangement(LocalDateTime.now());
            historique.setIdStatus(statusOpt.get());
            historique.setMessage(savedMessage);
            
            historiqueRepository.save(historique);
            
            // DÉTERMINER LE NIVEAU D'ALERTE
            String statusText = statusOpt.get().getStatus();
            String interventionText = interventionOpt.get().getIntervention();

            System.out.println("🔍 Debug Alerte - Status: " + statusText + 
                  ", Intervention: " + interventionText + 
                  ", Renfort: " + renfort);
            
            String niveau = niveauAlerteService.traiterAlerteComplete(
                statusText, interventionText, renfort, idSite, savedMessage.getIdMessage());

            // ENVOYER LES ALERTES AUX FONCTIONS CONCERNÉES
            envoyerAlerteAuxFonctionsConcernées(niveau, savedMessage);

            emailService.envoyerAlertesPourZone(niveau);
            

            // GÉNÉRER ET ENVOYER L'INFO DE RETOUR
            String infoRetour = infoRetourService.genererInfoRetour(savedMessage);
            // smsResponseService.sendResponse(phoneNumber, infoRetour);
            
            return infoRetour;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur détaillée lors du traitement de l'alerte: " + e.getMessage());
            e.printStackTrace();
            return "❌ Erreur lors du traitement de l'alerte: " + e.getMessage();
        } 
    }

    private boolean isDuplicateMessage(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return false;
        }
        return messageRepository.existsByLongitudeAndLatitude(longitude, latitude);
    }

    // Méthodes utilitaires pour le parsing
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (value.length() <= 10) {
                        return LocalDateTime.parse(value + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } else {
                        return LocalDateTime.parse(value, formatter);
                    }
                } catch (DateTimeParseException e) {
                    // Continuer avec le formateur suivant
                }
            }
            return null;
        } catch (Exception e) {
            return null;
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
    
    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        String lowerValue = value.trim().toLowerCase();
        return "true".equals(lowerValue) || "1".equals(lowerValue) || "oui".equals(lowerValue);
    }
    
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("❌ Impossible de convertir en Double : '" + value + "'");
            return null;
        }
    }
    
    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim();
    }

    private Map<String, String> parseKeyValueMessage(String message) {
        Map<String, String> fields = new HashMap<>();
        if (message == null || message.isBlank()) {
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

            String rawValue = keyValue[1] == null ? "" : keyValue[1].trim();
            fields.put(key, decodeValue(rawValue));
        }
        return fields;
    }

    private String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    // Ajoutez cette nouvelle méthode
    private void envoyerAlerteAuxFonctionsConcernées(String niveauAlerte, Message message) {
        try {
            // 1. Trouver les fonctions associées à cette zone d'alerte
            List<FonctionZoneAlerte> fonctionsZone = fonctionZoneAlerteRepository.findByTypeAlerteZone(niveauAlerte);
            
            if (fonctionsZone.isEmpty()) {
                System.out.println("⚠️ Aucune fonction trouvée pour la zone: " + niveauAlerte);
                return;
            }
            
            // 2. Récupérer les IDs des fonctions concernées
            List<Integer> idFonctions = fonctionsZone.stream()
                .map(fza -> fza.getFonction().getIdFonction())
                .collect(Collectors.toList());
            
            // 3. Trouver les utilisateurs ayant ces fonctions
            List<User> usersConcernes = userRepository.findByFonctionIdIn(idFonctions);
            
            if (usersConcernes.isEmpty()) {
                System.out.println("⚠️ Aucun utilisateur trouvé pour les fonctions: " + idFonctions);
                return;
            }
            
            // 4. Récupérer les numéros de téléphone
            List<String> numerosTelephone = usersConcernes.stream()
                .map(User::getTelephone)
                .filter(tel -> tel != null && !tel.trim().isEmpty())
                .collect(Collectors.toList());
            
            // 5. Préparer le message détaillé
            String messageDetaille = genererMessageDetailleAlerte(message, niveauAlerte);
            
            // 6. Envoyer les SMS
            for (String numero : numerosTelephone) {
                try {
                    smsResponseService.sendResponseSansChiffre(numero, messageDetaille);
                    System.out.println("✅ Alerte envoyée à: " + numero);
                } catch (Exception e) {
                    System.err.println("❌ Erreur envoi alerte à " + numero + ": " + e.getMessage());
                }
            }
            
            System.out.println("📨 Alertes envoyées à " + numerosTelephone.size() + " utilisateurs pour la zone: " + niveauAlerte);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi des alertes aux fonctions: " + e.getMessage());
        }
    }

    // Ajoutez cette méthode pour générer le message détaillé
    private String genererMessageDetailleAlerte(Message message, String niveauAlerte) {
        return String.format("""
            🚨 ALERTE %s
            Localisation: %.6f, %.6f
            Début: %s
            Intervention: %s
            Surface: %s m²
            Direction: %s
            Description: %s
            Renfort: %s
            """,
            niveauAlerte,
            message.getLatitude(),
            message.getLongitude(),
            message.getDateCommencement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            message.getIntervention().getIntervention(),
            message.getSurfaceApproximative() != null ? message.getSurfaceApproximative().toString() : "Non spécifiée",
            message.getDirection(),
            message.getDescription() != null ? message.getDescription() : "Aucune description",
            message.getRenfort() != null && message.getRenfort() ? "Oui" : "Non"
        );
    }
    
}