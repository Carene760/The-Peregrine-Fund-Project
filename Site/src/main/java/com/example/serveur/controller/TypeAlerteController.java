package com.example.serveur.controller;

import com.example.serveur.model.TypeAlerte;
import com.example.serveur.service.TypeAlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/types-alerte")
public class TypeAlerteController {

    private final TypeAlerteService typeAlerteService;

    @Autowired
    public TypeAlerteController(TypeAlerteService typeAlerteService) {
        this.typeAlerteService = typeAlerteService;
    }

    // Liste de tous les types d'alerte
    @GetMapping
    public String listTypesAlerte(Model model) {
        List<TypeAlerte> types = typeAlerteService.findAll();
        model.addAttribute("types", types);
        return "types-alerte/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("typeAlerte", new TypeAlerte());
        return "types-alerte/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un type d'alerte
    @PostMapping("/add")
    public String addTypeAlerte(@ModelAttribute TypeAlerte typeAlerte) {
        typeAlerteService.save(typeAlerte);
        return "redirect:/types-alerte";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        TypeAlerte typeAlerte = typeAlerteService.findById(id)
                .orElseThrow(() -> new RuntimeException("Type d'alerte non trouvé"));
        model.addAttribute("typeAlerte", typeAlerte);
        return "types-alerte/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un type d'alerte
    // @PostMapping("/edit/{id}")
    // public String editTypeAlerte(@PathVariable int id, @ModelAttribute TypeAlerte typeAlerte) {
    //     typeAlerteService.save(typeAlerteService.setId(typeAlerte, id));
    //     return "redirect:/types-alerte";
    // }

    // Supprimer un type d'alerte
    @GetMapping("/delete/{id}")
    public String deleteTypeAlerte(@PathVariable int id) {
        typeAlerteService.deleteById(id);
        return "redirect:/types-alerte";
    }
}
