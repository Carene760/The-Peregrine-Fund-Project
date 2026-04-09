package com.example.serveur.controller.Historique;

import com.example.serveur.model.*;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.EvenementRepository;
import com.example.serveur.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

@Controller
public class HistoriqueController {

    private final MessageService messageService;
    private final AlerteService alerteService;
    private final HistoriqueMessageStatusService historiqueMessageStatusService;
    private final SiteService siteService;
    private final HistoriqueMessageEditService historiqueMessageEditService;
    private final InterventionRepository interventionRepository;
    private final EvenementRepository evenementRepository;

    @Autowired
    public HistoriqueController(MessageService messageService,
                                AlerteService alerteService,
                                HistoriqueMessageStatusService historiqueMessageStatusService,
                                SiteService siteService,
                                HistoriqueMessageEditService historiqueMessageEditService,
                                InterventionRepository interventionRepository,
                                EvenementRepository evenementRepository) {
        this.messageService = messageService;
        this.alerteService = alerteService;
        this.historiqueMessageStatusService = historiqueMessageStatusService;
        this.siteService = siteService;
        this.historiqueMessageEditService = historiqueMessageEditService;
        this.interventionRepository = interventionRepository;
        this.evenementRepository = evenementRepository;
    }

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        // Récupération des données
        List<Message> messages = messageService.findAll();
        List<Alerte> alertes = alerteService.findAll();
        List<HistoriqueMessageStatus> status = historiqueMessageStatusService.findAll();
        List<Site> sites = siteService.findAll();
        List<Intervention> interventions = interventionRepository.findAll();
        List<Evenement> evenements = evenementRepository.findAll();

        // Associer chaque message avec ses alertes
        Map<Integer, List<Alerte>> alerteMessage = new HashMap<>();
        for (Alerte a : alertes) {
            alerteMessage.computeIfAbsent(a.getMessage().getIdMessage(), k -> new ArrayList<>()).add(a);
        }

        // Associer chaque message avec ses historiques de statut
        Map<Integer, List<HistoriqueMessageStatus>> statusMessage = new HashMap<>();
        for (HistoriqueMessageStatus s : status) {
            statusMessage.computeIfAbsent(s.getMessage().getIdMessage(), k -> new ArrayList<>()).add(s);
        }

        // Extraire uniquement le dernier statut par message
        Map<Integer, HistoriqueMessageStatus> dernierStatusMessage = new HashMap<>();
        for (Map.Entry<Integer, List<HistoriqueMessageStatus>> entry : statusMessage.entrySet()) {
            int messageId = entry.getKey();
            List<HistoriqueMessageStatus> historiques = entry.getValue();

            HistoriqueMessageStatus dernier = historiques.stream()
                    .max(Comparator.comparing(HistoriqueMessageStatus::getDateChangement))
                    .orElse(null);

            if (dernier != null) {
                dernierStatusMessage.put(messageId, dernier);
            }
        }

        // Passer les données au modèle
        model.addAttribute("user", currentUser);
        model.addAttribute("messages", messages);
        model.addAttribute("alerteMessage", alerteMessage);
        model.addAttribute("statusMessage", dernierStatusMessage);
        model.addAttribute("sites", sites);
        model.addAttribute("interventions", interventions);
        model.addAttribute("evenements", evenements);
        model.addAttribute("directions", new String[]{"N", "NE", "E", "SE", "S", "SO", "O", "NO"});

        return "historique";
    }

    @PostMapping("/history/messages/{id}")
    @ResponseBody
    public Map<String, Object> updateMessage(@PathVariable("id") int messageId,
                                             @RequestBody Map<String, Object> payload,
                                             HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        Map<String, Object> response = new HashMap<>();
        
        if (!historiqueMessageEditService.isAdmin(currentUser)) {
            response.put("success", false);
            response.put("message", "Action reservee a l administrateur");
            return response;
        }

        boolean updated = historiqueMessageEditService.updateMessageAsAdmin(currentUser, messageId, payload);
        response.put("success", updated);
        response.put("message", updated ? "Message modifie" : "Message introuvable");
        return response;
    }

}

