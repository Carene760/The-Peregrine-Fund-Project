package com.example.serveur.controller;

import com.example.serveur.model.Intervention;
import com.example.serveur.service.InterventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/interventions")
public class InterventionController {

    private final InterventionService interventionService;

    @Autowired
    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    // Liste de toutes les interventions
    @GetMapping
    public String listInterventions(Model model) {
        List<Intervention> interventions = interventionService.findAll();
        model.addAttribute("interventions", interventions);
        return "interventions/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("intervention", new Intervention());
        return "interventions/add"; // Vue Thymeleaf à créer
    }

    // Ajouter une intervention
    @PostMapping("/add")
    public String addIntervention(@ModelAttribute Intervention intervention) {
        interventionService.save(intervention);
        return "redirect:/interventions";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Intervention intervention = interventionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));
        model.addAttribute("intervention", intervention);
        return "interventions/edit"; // Vue Thymeleaf à créer
    }

    // Modifier une intervention
    // @PostMapping("/edit/{id}")
    // public String editIntervention(@PathVariable int id, @ModelAttribute Intervention intervention) {
    //     interventionService.save(interventionService.setId(intervention, id));
    //     return "redirect:/interventions";
    // }

    // Supprimer une intervention
    @GetMapping("/delete/{id}")
    public String deleteIntervention(@PathVariable int id) {
        interventionService.deleteById(id);
        return "redirect:/interventions";
    }
}
