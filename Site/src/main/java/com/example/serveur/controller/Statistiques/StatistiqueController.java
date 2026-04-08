package com.example.serveur.controller.Statistiques;

import com.example.serveur.model.*;
import com.example.serveur.service.*;
import com.example.serveur.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class StatistiqueController {

    private final SiteService siteService;
    private final AlerteRepository alerteRepository;
    private final MessageService messageService;

    @Autowired
    public StatistiqueController(SiteService siteService, AlerteRepository alerteRepository, 
                               MessageService messageService ) {
        this.siteService = siteService;
        this.alerteRepository = alerteRepository;
        this.messageService = messageService;
    }

    @GetMapping("/stat")
    public String showStatistiques(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        // Tous les sites avec leurs messages
        List<Site> sites = siteService.findAll();
        
        // Créer une structure de données pour les sites avec leurs messages
        List<SiteWithMessages> sitesWithMessages = new ArrayList<>();
        
        for (Site site : sites) {
            // Récupérer tous les messages pour ce site
            List<Message> messagesForSite = messageService.findMessagesBySite(site.getId_Site());
            
            // Filtrer les messages qui ont des coordonnées valides
            List<MessageCoordinates> messageCoordinates = messagesForSite.stream()
                .filter(msg -> msg.getLatitude() != null && msg.getLongitude() != null)
                .map(msg -> new MessageCoordinates(
                    msg.getLatitude(),
                    msg.getLongitude(),
                    msg.getDescription(),
                    msg.getPointRepere(),
                    msg.getDateSignalement().toString(),
                    msg.getIntervention().getIntervention(), // Type d'intervention
                    msg.getUserApp().getPatrouilleur().getNom() // Nom du patrouilleur
                ))
                .collect(Collectors.toList());
            
            sitesWithMessages.add(new SiteWithMessages(
                site.getId_Site(),
                site.getNom(),
                site.getLatitude(),
                site.getLongitude(),
                site.getRegion(),
                messageCoordinates
            ));
        }

        // Répartition des alertes par type
        List<Object[]> stats = alerteRepository.countAlertesByType();
        Map<String, Integer> repartitionAlertes = new HashMap<>();
        for (Object[] row : stats) {
            Integer typeId = (Integer) row[0];
            String type = String.valueOf(typeId);
            Long countLong = (Long) row[1];         // Récupérer en Long
            int count = countLong.intValue();
            repartitionAlertes.put(type, count);
        }

        // Messages par site
        List<Object[]> messageStats = messageService.countMessagesBySite();
        Map<String, Integer> messageCountBySite = new HashMap<>();
        for (Object[] row : messageStats) {
            String site = String.valueOf(row[0]);
            int count = ((Number) row[1]).intValue();
            messageCountBySite.put(site, count);

        }

        model.addAttribute("user", currentUser);
        model.addAttribute("sites", sites);
        model.addAttribute("sitesWithMessages", sitesWithMessages);
        model.addAttribute("repartitionAlertes", repartitionAlertes);
        model.addAttribute("messageCountBySite", messageCountBySite);

        return "statistiques";
    }

    // Classes internes pour structurer les données
    public static class SiteWithMessages {
        private int id;
        private String nom;
        private Double latitude;
        private Double longitude;
        private String region;
        private List<MessageCoordinates> messages;

        public SiteWithMessages(int id, String nom, Double latitude, Double longitude, 
                              String region, List<MessageCoordinates> messages) {
            this.id = id;
            this.nom = nom;
            this.latitude = latitude;
            this.longitude = longitude;
            this.region = region;
            this.messages = messages;
        }

        // Getters
        public int getId() { return id; }
        public String getNom() { return nom; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getRegion() { return region; }
        public List<MessageCoordinates> getMessages() { return messages; }
    }

    public static class MessageCoordinates {
        private Double latitude;
        private Double longitude;
        private String description;
        private String pointRepere;
        private String dateSignalement;
        private String typeAlerte;
        private String patrouilleur;

        public MessageCoordinates(Double latitude, Double longitude, String description, 
                                String pointRepere, String dateSignalement, String typeAlerte, String patrouilleur) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.description = description;
            this.pointRepere = pointRepere;
            this.dateSignalement = dateSignalement;
            this.typeAlerte = typeAlerte;
            this.patrouilleur = patrouilleur;
        }

        // Getters
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getDescription() { return description; }
        public String getPointRepere() { return pointRepere; }
        public String getDateSignalement() { return dateSignalement; }
        public String getTypeAlerte() { return typeAlerte; }
        public String getPatrouilleur() { return patrouilleur; }
    }
}