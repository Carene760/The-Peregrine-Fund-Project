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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.time.ZoneId;
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
    private final DeadLetterService deadLetterService;

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
                        UserRepository userRepository,
                        DeadLetterService deadLetterService) {
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
        this.deadLetterService = deadLetterService;
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
                    return reject(phoneNumber, messageAlerte,
                            "Format invalide: " + (parties.length - 1) + " séparateurs trouvés (12 attendus)");
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
                return reject(phoneNumber, messageAlerte, "Date de commencement manquante ou invalide");
            }
            if (dateSignalement == null) {
                return reject(phoneNumber, messageAlerte, "Date de signalement manquante ou invalide");
            }
            if (idIntervention == null) {
                return reject(phoneNumber, messageAlerte, "ID intervention manquant ou invalide");
            }
            if (idUserApp == null) {
                return reject(phoneNumber, messageAlerte, "ID UserApp manquant ou invalide");
            }
            if (idStatus == null) {
                return reject(phoneNumber, messageAlerte, "ID Status manquant ou invalide");
            }

            if (!dateSignalement.isAfter(dateCommencement)) {
                if (isV2Format && dateCommencement.isAfter(dateSignalement)) {
                    // Compatibilite: certains clients V2 inversent ces deux champs.
                    LocalDateTime temp = dateCommencement;
                    dateCommencement = dateSignalement;
                    dateSignalement = temp;
                    System.out.println("⚠️ Dates inversées détectées en V2: correction appliquée");
                }
            }

            if (!dateSignalement.isAfter(dateCommencement)) {
                return reject(phoneNumber, messageAlerte, "Incohérence des dates: dateSignalement doit être > dateCommencement");
            }

            System.out.println("✅ Données parsées - ID UserApp: " + idUserApp);

            if (isDuplicateMessage(longitude, latitude)) {
                return "⚠️ Message déjà existant - Doublon ignoré";
            }

            // Vérifier que les entités référencées existent
            Optional<Intervention> interventionOpt = interventionRepository.findById(idIntervention);
            if (interventionOpt.isEmpty()) {
                return reject(phoneNumber, messageAlerte, "Intervention non trouvée avec ID: " + idIntervention);
            }

            Optional<UserApp> userAppOpt = userAppRepository.findById(idUserApp);
            if (userAppOpt.isEmpty()) {
                return reject(phoneNumber, messageAlerte, "UserApp non trouvé avec ID: " + idUserApp);
            }

            Optional<StatusMessage> statusOpt = statusMessageRepository.findById(idStatus);
            if (statusOpt.isEmpty()) {
                return reject(phoneNumber, messageAlerte, "Status non trouvé avec ID: " + idStatus);
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
            // Hors du fil de la requête: passe par la passerelle SMS (RestTemplate,
            // gateway.internal-send-url) qui peut être injoignable sur le terrain -
            // même bornée par un timeout (voir ServeurApplication.restTemplate()),
            // une attente de plusieurs secondes ici retarderait inutilement la
            // réponse HTTP renvoyée au ranger, alors que son alerte est déjà
            // enregistrée en base à ce stade (voir test de charge).
            CompletableFuture.runAsync(() -> envoyerAlerteAuxFonctionsConcernées(niveau, savedMessage));

            emailService.envoyerAlertesPourZone(niveau, savedMessage);


            // GÉNÉRER ET ENVOYER L'INFO DE RETOUR
            String infoRetour = infoRetourService.genererInfoRetour(savedMessage);
            // smsResponseService.sendResponse(phoneNumber, infoRetour);
            
            return infoRetour;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur détaillée lors du traitement de l'alerte: " + e.getMessage());
            e.printStackTrace();
            deadLetterService.record("AlerteService.processAlerte", phoneNumber,
                    "Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage(), messageAlerte);
            return "❌ Erreur lors du traitement de l'alerte";
        }
    }

    /** Records a malformed/rejected alert in the dead-letter log before returning the user-facing reason. */
    private String reject(String phoneNumber, String rawMessage, String reason) {
        deadLetterService.record("AlerteService.processAlerte", phoneNumber, reason, rawMessage);
        return "❌ " + reason;
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
            String normalized = value.trim();

            try {
                return OffsetDateTime.parse(normalized)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (DateTimeParseException e) {
                // Fallback sur les autres parseurs
            }

            try {
                return Instant.parse(normalized)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (DateTimeParseException e) {
                // Fallback sur les autres parseurs
            }

            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (normalized.length() <= 10) {
                        return LocalDateTime.parse(normalized + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } else {
                        return LocalDateTime.parse(normalized, formatter);
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

    /**
     * Trouve les numéros de téléphone des utilisateurs dont la fonction est
     * concernée par une zone d'alerte donnée (vert/jaune/orange/rouge).
     * Factorisé pour être réutilisé à la fois pour l'alerte initiale
     * (envoyerAlerteAuxFonctionsConcernées) et pour la notification de
     * changement de statut (notifierChangementStatut).
     */
    private List<String> telephonesPourZone(String niveauAlerte) {
        List<FonctionZoneAlerte> fonctionsZone = fonctionZoneAlerteRepository.findByTypeAlerteZone(niveauAlerte);
        if (fonctionsZone.isEmpty()) {
            System.out.println("⚠️ Aucune fonction trouvée pour la zone: " + niveauAlerte);
            return List.of();
        }

        List<Integer> idFonctions = fonctionsZone.stream()
            .map(fza -> fza.getFonction().getIdFonction())
            .collect(Collectors.toList());

        List<User> usersConcernes = userRepository.findByFonctionIdIn(idFonctions);
        if (usersConcernes.isEmpty()) {
            System.out.println("⚠️ Aucun utilisateur trouvé pour les fonctions: " + idFonctions);
            return List.of();
        }

        return usersConcernes.stream()
            .map(User::getTelephone)
            .filter(tel -> tel != null && !tel.trim().isEmpty())
            .collect(Collectors.toList());
    }

    private void envoyerSmsAFonctionsConcernées(String niveauAlerte, String messageDetaille) {
        List<String> numerosTelephone = telephonesPourZone(niveauAlerte);
        for (String numero : numerosTelephone) {
            try {
                smsResponseService.sendResponseSansChiffre(numero, messageDetaille);
                System.out.println("✅ Notification envoyée à: " + numero);
            } catch (Exception e) {
                System.err.println("❌ Erreur envoi notification à " + numero + ": " + e.getMessage());
            }
        }
        System.out.println("📨 Notifications envoyées à " + numerosTelephone.size() + " utilisateurs pour la zone: " + niveauAlerte);
    }

    // Ajoutez cette nouvelle méthode
    private void envoyerAlerteAuxFonctionsConcernées(String niveauAlerte, Message message) {
        try {
            String messageDetaille = genererMessageDetailleAlerte(message, niveauAlerte);
            envoyerSmsAFonctionsConcernées(niveauAlerte, messageDetaille);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi des alertes aux fonctions: " + e.getMessage());
        }
    }

    /**
     * Notifie les mêmes destinataires que l'alerte initiale (fonctions
     * concernées par SMS + email de zone) qu'un changement de statut est
     * survenu sur un message déjà signalé (ex: "Debut de feu" -> "Maitrise").
     *
     * Contrairement à processAlerte()/niveauAlerteService.traiterAlerteComplete,
     * cette méthode NE crée PAS de nouvel enregistrement Alerte: elle
     * réutilise le niveau/la zone déjà déterminés à la création de l'alerte
     * initiale (retrouvée via AlerteRepository), pour éviter de dupliquer
     * les statistiques d'alertes à chaque changement de statut.
     *
     * Appelée depuis HistoriqueMessageStatusService.updateMessageStatus()
     * après un changement de statut réussi - avant ce correctif, un
     * changement de statut n'était jamais notifié à personne d'autre que,
     * éventuellement, l'expéditeur lui-même (accusé SMS optionnel).
     */
    public void notifierChangementStatut(Message message, String nouveauStatut) {
        alerteRepository.findTopByMessage_IdMessageOrderByIdAlerteDesc(message.getIdMessage())
            .ifPresentOrElse(alerte -> {
                String niveau = alerte.getTypeAlerte().getZone();
                String messageDetaille = genererMessageChangementStatut(message, niveau, nouveauStatut);
                CompletableFuture.runAsync(() -> envoyerSmsAFonctionsConcernées(niveau, messageDetaille));
                emailService.envoyerAlertesPourZone(niveau, message);
            }, () -> System.out.println(
                "⚠️ Pas d'alerte associée au message " + message.getIdMessage()
                    + ", notification de changement de statut ignorée"));
    }

    private String pictoZone(String niveauAlerte) {
        if (niveauAlerte == null) return "⚪";
        switch (niveauAlerte.toLowerCase()) {
            case "vert": return "🟢";
            case "jaune": return "🟡";
            case "orange": return "🟠";
            case "rouge": return "🔴";
            default: return "⚪";
        }
    }

    private String genererMessageChangementStatut(Message message, String niveauAlerte, String nouveauStatut) {
        return String.format("""
            %s MISE À JOUR - Niveau %s
            Nouveau statut : %s
            Point de repère : %s
            Intervention : %s
            Localisation : %.6f, %.6f
            """,
            pictoZone(niveauAlerte),
            niveauAlerte,
            nouveauStatut,
            message.getPointRepere() != null && !message.getPointRepere().isBlank() ? message.getPointRepere() : "Non renseigné",
            message.getIntervention() != null ? message.getIntervention().getIntervention() : "Non renseignée",
            message.getLatitude() != null ? message.getLatitude() : 0,
            message.getLongitude() != null ? message.getLongitude() : 0
        );
    }

    // Ajoutez cette méthode pour générer le message détaillé
    private String genererMessageDetailleAlerte(Message message, String niveauAlerte) {
        return String.format("""
            %s ALERTE INCENDIE - Niveau %s
            Point de repère : %s
            Intervention : %s / Renfort : %s
            Début : %s
            Surface estimée : %s
            Direction : %s
            Localisation : %.6f, %.6f
            Description : %s
            """,
            pictoZone(niveauAlerte),
            niveauAlerte,
            message.getPointRepere() != null && !message.getPointRepere().isBlank() ? message.getPointRepere() : "Non renseigné",
            message.getIntervention() != null ? message.getIntervention().getIntervention() : "Non renseignée",
            message.getRenfort() != null && message.getRenfort() ? "Oui" : "Non",
            message.getDateCommencement() != null ? message.getDateCommencement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Non renseigné",
            message.getSurfaceApproximative() != null ? message.getSurfaceApproximative() + " m²" : "Non renseignée",
            message.getDirection() != null && !message.getDirection().isBlank() ? message.getDirection() : "Non renseignée",
            message.getLatitude() != null ? message.getLatitude() : 0,
            message.getLongitude() != null ? message.getLongitude() : 0,
            message.getDescription() != null && !message.getDescription().isBlank() ? message.getDescription() : "Aucune description fournie"
        );
    }

}
