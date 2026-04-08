package com.example.serveur.controller;

import com.example.serveur.model.HistoriqueStatusAgent;
import com.example.serveur.service.HistoriqueStatusAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/historique-status-agent")
public class HistoriqueStatusAgentController {

    private final HistoriqueStatusAgentService historiqueStatusAgentService;

    @Autowired
    public HistoriqueStatusAgentController(HistoriqueStatusAgentService historiqueStatusAgentService) {
        this.historiqueStatusAgentService = historiqueStatusAgentService;
    }

    // Liste de tous les historiques de status agent
    @GetMapping
    public String listHistorique(Model model) {
        List<HistoriqueStatusAgent> historiques = historiqueStatusAgentService.findAll();
        model.addAttribute("historiques", historiques);
        return "historique-status-agent/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("historique", new HistoriqueStatusAgent());
        return "historique-status-agent/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un historique
    @PostMapping("/add")
    public String addHistorique(@ModelAttribute HistoriqueStatusAgent historique) {
        historiqueStatusAgentService.save(historique);
        return "redirect:/historique-status-agent";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        HistoriqueStatusAgent historique = historiqueStatusAgentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Historique non trouvé"));
        model.addAttribute("historique", historique);
        return "historique-status-agent/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un historique
    // @PostMapping("/edit/{id}")
    // public String editHistorique(@PathVariable int id, @ModelAttribute HistoriqueStatusAgent historique) {
    //     historiqueStatusAgentService.save(historiqueStatusAgentService.setId(historique, id));
    //     return "redirect:/historique-status-agent";
    // }

    // Supprimer un historique
    @GetMapping("/delete/{id}")
    public String deleteHistorique(@PathVariable int id) {
        historiqueStatusAgentService.deleteById(id);
        return "redirect:/historique-status-agent";
    }
}
