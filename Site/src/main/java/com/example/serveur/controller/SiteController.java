package com.example.serveur.controller;

import com.example.serveur.model.Site;
import com.example.serveur.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/sites")
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    // Liste de tous les sites
    @GetMapping
    public String listSites(Model model) {
        List<Site> sites = siteService.findAll();
        model.addAttribute("sites", sites);
        return "sites/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("site", new Site());
        return "sites/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un site
    @PostMapping("/add")
    public String addSite(@ModelAttribute Site site) {
        siteService.save(site);
        return "redirect:/sites";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Site site = siteService.findById(id)
                .orElseThrow(() -> new RuntimeException("Site non trouvé"));
        model.addAttribute("site", site);
        return "sites/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un site
    // @PostMapping("/edit/{id}")
    // public String editSite(@PathVariable int id, @ModelAttribute Site site) {
    //     siteService.save(siteService.setId(site, id));
    //     return "redirect:/sites";
    // }

    // Supprimer un site
    @GetMapping("/delete/{id}")
    public String deleteSite(@PathVariable int id) {
        siteService.deleteById(id);
        return "redirect:/sites";
    }
}
