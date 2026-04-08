package com.example.serveur.controller;

import com.example.serveur.model.Fonction;
import com.example.serveur.service.FonctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/fonctions")
public class FonctionController {

    private final FonctionService fonctionService;

    @Autowired
    public FonctionController(FonctionService fonctionService) {
        this.fonctionService = fonctionService;
    }

    // Liste de toutes les fonctions
    @GetMapping
    public String listFonctions(Model model) {
        List<Fonction> fonctions = fonctionService.findAll();
        model.addAttribute("fonctions", fonctions);
        return "fonctions/list";  // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("fonction", new Fonction());
        return "fonctions/add";  // Vue Thymeleaf à créer
    }

    // Ajouter une fonction
    @PostMapping("/add")
    public String addFonction(@ModelAttribute Fonction fonction) {
        fonctionService.save(fonction);
        return "redirect:/fonctions";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Fonction fonction = fonctionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Fonction non trouvée"));
        model.addAttribute("fonction", fonction);
        return "fonctions/edit";  // Vue Thymeleaf à créer
    }

    // Modifier une fonction
    // @PostMapping("/edit/{id}")
    // public String editFonction(@PathVariable int id, @ModelAttribute Fonction fonction) {
    //     fonctionService.save(fonctionService.setId(fonction, id));
    //     return "redirect:/fonctions";
    // }

    // Supprimer une fonction
    @GetMapping("/delete/{id}")
    public String deleteFonction(@PathVariable int id) {
        fonctionService.deleteById(id);
        return "redirect:/fonctions";
    }
}
