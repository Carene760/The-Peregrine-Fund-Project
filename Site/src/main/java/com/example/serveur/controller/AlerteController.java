package com.example.serveur.controller;

import com.example.serveur.model.Alerte;
import com.example.serveur.service.AlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/alertes")
public class AlerteController {

    private final AlerteService alerteService;

    @Autowired
    public AlerteController(AlerteService alerteService) {
        this.alerteService = alerteService;
    }

    // Liste de toutes les alertes
    @GetMapping
    public String listAlertes(Model model) {
        List<Alerte> alertes = alerteService.findAll();
        model.addAttribute("alertes", alertes);
        return "alertes/list";  // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("alerte", new Alerte());
        return "alertes/add";  // Vue Thymeleaf à créer
    }

    // Ajouter une alerte
    @PostMapping("/add")
    public String addAlerte(@ModelAttribute Alerte alerte) {
        alerteService.save(alerte);
        return "redirect:/alertes";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Alerte alerte = alerteService.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        model.addAttribute("alerte", alerte);
        return "alertes/edit";  // Vue Thymeleaf à créer
    }

    // Modifier une alerte
    // @PostMapping("/edit/{id}")
    // public String editAlerte(@PathVariable int id, @ModelAttribute Alerte alerte) {
    //     alerte.setId(id);
    //     alerteService.save(alerte);
    //     return "redirect:/alertes";
    // }

    // Supprimer une alerte
    @GetMapping("/delete/{id}")
    public String deleteAlerte(@PathVariable int id) {
        alerteService.deleteById(id);
        return "redirect:/alertes";
    }
}
