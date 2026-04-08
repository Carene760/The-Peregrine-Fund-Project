package com.example.serveur.service;

import com.example.serveur.model.Message;
import com.example.serveur.model.TypeAlerte;
import com.example.serveur.model.Site;
import com.example.serveur.repository.AlerteRepository;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.repository.TypeAlerteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InfoRetourService {
    
    private final MessageRepository messageRepository;
    private final AlerteRepository alerteRepository;
    private final String separateurInfo;
    private final TypeAlerteRepository typeAlerteRepository;
    
    public InfoRetourService(MessageRepository messageRepository,
                           AlerteRepository alerteRepository,
                           TypeAlerteRepository typeAlerteRepository,
                           @Value("${info.separator}") String separateurInfo) {
        this.messageRepository = messageRepository;
        this.alerteRepository = alerteRepository;
        this.typeAlerteRepository = typeAlerteRepository;
        this.separateurInfo = separateurInfo;
    }
    
    /**
     * Génère le message d'information de retour
     */
    public String genererInfoRetour(Message message) {
        try {
            // 1. Latitude/Longitude (depuis le message)
            String localisation = getLocalisation(message);
            
            // 2. Pourcentage par niveau d'alerte
            String pourcentageAlertes = getPourcentageParNiveauAlerte();
            
            // 3. Nombre de messages par site
            String messagesParSite = getMessagesParSite();
            
            // Combiner les 3 parties avec le séparateur
            return localisation + separateurInfo + pourcentageAlertes + separateurInfo + messagesParSite;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération info retour: " + e.getMessage());
            return "Erreur génération information";
        }
    }
    
    /**
     * 1. Localisation: latitude/longitude
     */
    private String getLocalisation(Message message) {
        Site site = message.getUserApp().getPatrouilleur().getSite();
        if (site.getLatitude() != null && site.getLongitude() != null) {
            return message.getLatitude() + "/" + message.getLongitude();
        }
        return "0/0"; // Valeur par défaut si null
    }
    
    /**
     * 2. Pourcentage par niveau d'alerte (format: vert/jaune/orange/rouge)
     */
    private String getPourcentageParNiveauAlerte() {
        try {
            // Compter le total des alertes
            long totalAlertes = alerteRepository.count();
            
            if (totalAlertes == 0) {
                return "0/0/0/0"; // Aucune alerte en base
            }
            
            // Compter par niveau d'alerte
            Map<String, Long> countByNiveau = alerteRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    alerte -> getNiveauAlerte(alerte.getTypeAlerte().getIdTypeAlerte()),
                    Collectors.counting()
                ));
            
            // Calculer les pourcentages
            long vert = countByNiveau.getOrDefault("Vert", 0L);
            long jaune = countByNiveau.getOrDefault("Jaune", 0L);
            long orange = countByNiveau.getOrDefault("Orange", 0L);
            long rouge = countByNiveau.getOrDefault("Rouge", 0L);
            
            // Calculer les pourcentages
            int pctVert = (int) Math.round((vert * 100.0) / totalAlertes);
            int pctJaune = (int) Math.round((jaune * 100.0) / totalAlertes);
            int pctOrange = (int) Math.round((orange * 100.0) / totalAlertes);
            int pctRouge = (int) Math.round((rouge * 100.0) / totalAlertes);
            
            return pctVert + "/" + pctJaune + "/" + pctOrange + "/" + pctRouge;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur calcul pourcentages: " + e.getMessage());
            return "0/0/0/0";
        }
    }
    
    /**
     * 3. Nombre de messages par site (format: site1?10/site2?15/...)
     */
    private String getMessagesParSite() {
        try {
            // Récupérer le nombre de messages par site
            List<Object[]> results = messageRepository.countMessagesBySite();
            
            if (results.isEmpty()) {
                return "Aucun message";
            }
            
            // Formater les résultats
            return results.stream()
                .map(result -> "site" + result[0] + "?" + result[1])
                .collect(Collectors.joining("/"));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage messages par site: " + e.getMessage());
            return "Erreur comptage";
        }
    }
    
    /**
     * Helper pour obtenir le niveau d'alerte à partir de l'ID
     */
    private String getNiveauAlerte(Integer idTypeAlerte) {
        if (idTypeAlerte == null) return "Inconnu";
        
        try {
            // Utilisation du repository pour trouver le TypeAlerte par son ID
            Optional<TypeAlerte> typeAlerteOpt = typeAlerteRepository.findById(idTypeAlerte);
            
            if (typeAlerteOpt.isPresent()) {
                return typeAlerteOpt.get().getZone();
            } else {
                System.err.println("❌ TypeAlerte non trouvé pour l'ID: " + idTypeAlerte);
                return "Inconnu";
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du TypeAlerte: " + e.getMessage());
            return "Inconnu";
        }
    }
}