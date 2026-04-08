package com.example.serveur.controller;

import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.service.HistoriqueMessageStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/historique-message-status")
public class HistoriqueMessageStatusController {

    private final HistoriqueMessageStatusService historiqueMessageStatusService;

    @Autowired
    public HistoriqueMessageStatusController(HistoriqueMessageStatusService historiqueMessageStatusService) {
        this.historiqueMessageStatusService = historiqueMessageStatusService;
    }

    // Liste de tous les historiques
    @GetMapping
    public String listHistorique(Model model) {
        List<HistoriqueMessageStatus> historiques = historiqueMessageStatusService.findAll();
        model.addAttribute("historiques", historiques);
        return "historique-message-status/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("historique", new HistoriqueMessageStatus());
        return "historique-message-status/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un historique
    @PostMapping("/add")
    public String addHistorique(@ModelAttribute HistoriqueMessageStatus historique) {
        historiqueMessageStatusService.save(historique);
        return "redirect:/historique-message-status";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        HistoriqueMessageStatus historique = historiqueMessageStatusService.findById(id)
                .orElseThrow(() -> new RuntimeException("Historique non trouvé"));
        model.addAttribute("historique", historique);
        return "historique-message-status/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un historique
    // @PostMapping("/edit/{id}")
    // public String editHistorique(@PathVariable int id, @ModelAttribute HistoriqueMessageStatus historique) {
    //     historiqueMessageStatusService.save(historiqueMessageStatusService.setId(historique, id));
    //     return "redirect:/historique-message-status";
    // }

    // Supprimer un historique
    @GetMapping("/delete/{id}")
    public String deleteHistorique(@PathVariable int id) {
        historiqueMessageStatusService.deleteById(id);
        return "redirect:/historique-message-status";
    }
}
