package com.example.serveur.controller;

import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.service.PatrouilleurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/patrouilleurs")
public class PatrouilleurController {

    private final PatrouilleurService patrouilleurService;

    @Autowired
    public PatrouilleurController(PatrouilleurService patrouilleurService) {
        this.patrouilleurService = patrouilleurService;
    }

    // Liste de tous les patrouilleurs
    @GetMapping
    public String listPatrouilleurs(Model model) {
        List<Patrouilleurs> patrouilleurs = patrouilleurService.findAll();
        model.addAttribute("patrouilleurs", patrouilleurs);
        return "patrouilleurs/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("patrouilleur", new Patrouilleurs());
        return "patrouilleurs/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un patrouilleur
    @PostMapping("/add")
    public String addPatrouilleur(@ModelAttribute Patrouilleurs patrouilleur) {
        patrouilleurService.save(patrouilleur);
        return "redirect:/patrouilleurs";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Patrouilleurs patrouilleur = patrouilleurService.findById(id)
                .orElseThrow(() -> new RuntimeException("Patrouilleur non trouvé"));
        model.addAttribute("patrouilleur", patrouilleur);
        return "patrouilleurs/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un patrouilleur
    // @PostMapping("/edit/{id}")
    // public String editPatrouilleur(@PathVariable int id, @ModelAttribute Patrouilleur patrouilleur) {
    //     patrouilleurService.save(patrouilleurService.setId(patrouilleur, id));
    //     return "redirect:/patrouilleurs";
    // }

    // Supprimer un patrouilleur
    @GetMapping("/delete/{id}")
    public String deletePatrouilleur(@PathVariable int id) {
        patrouilleurService.deleteById(id);
        return "redirect:/patrouilleurs";
    }
}
