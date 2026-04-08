package com.example.serveur.controller.Historique;

import com.example.serveur.model.*;
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

    @Autowired
    public HistoriqueController(MessageService messageService, AlerteService alerteService, HistoriqueMessageStatusService historiqueMessageStatusService, SiteService siteService) {
        this.messageService = messageService;
        this.alerteService = alerteService;
        this.historiqueMessageStatusService = historiqueMessageStatusService;
        this.siteService = siteService;
    }

    @GetMapping("/history")
public String showHistory(HttpSession session, Model model) {
    User currentUser = (User) session.getAttribute("currentUser");

    // Récupération des données
    List<Message> messages = messageService.findAll();
    List<Alerte> alertes = alerteService.findAll();
    List<HistoriqueMessageStatus> status = historiqueMessageStatusService.findAll();
    List<Site> sites = siteService.findAll();

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

    return "historique";
}

}

