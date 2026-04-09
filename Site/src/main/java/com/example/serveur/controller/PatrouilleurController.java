package com.example.serveur.controller;

import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.model.User;
import com.example.serveur.model.UserApp;
import com.example.serveur.service.PatrouilleurService;
import com.example.serveur.service.SiteService;
import com.example.serveur.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/patrouilleurs")
public class PatrouilleurController {

    private final PatrouilleurService patrouilleurService;
    private final SiteService siteService;
    private final UserAppService userAppService;

    @Autowired
    public PatrouilleurController(PatrouilleurService patrouilleurService,
                                  SiteService siteService,
                                  UserAppService userAppService) {
        this.patrouilleurService = patrouilleurService;
        this.siteService = siteService;
        this.userAppService = userAppService;
    }

    // Liste de tous les patrouilleurs
    @GetMapping({"", "/list"})
    public String listPatrouilleurs(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Patrouilleurs> patrouilleurs = patrouilleurService.findAll();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("patrouilleurs", patrouilleurs);
        return "redirect:/agentslist";
    }

    // Formulaire d'ajout
    @GetMapping({"/add", "/form"})
    public String showAddForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("patrouilleur", new Patrouilleurs());
        model.addAttribute("sites", siteService.findAll());
        return "patrouilleur-form";
    }

    // Ajouter un patrouilleur
    @PostMapping({"/add", "/save"})
    public String addPatrouilleur(@ModelAttribute Patrouilleurs patrouilleur,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (patrouilleur.getSite() == null) {
                throw new RuntimeException("Le site est obligatoire");
            }
            patrouilleurService.save(patrouilleur);
            redirectAttributes.addFlashAttribute("successMessage", "Patrouilleur créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/patrouilleurs/form";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, HttpSession session) {
        Object currentUser = session.getAttribute("currentUser");
        Patrouilleurs patrouilleur = patrouilleurService.findById(id)
                .orElseThrow(() -> new RuntimeException("Patrouilleur non trouvé"));
        String agentLogin = userAppService.findByPatrouilleurId(id)
            .map(UserApp::getLogin)
            .orElse("");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("patrouilleur", patrouilleur);
        model.addAttribute("agentLogin", agentLogin);
        model.addAttribute("sites", siteService.findAll());
        return "agents-edit";
    }

    // Modifier un patrouilleur
    @PostMapping("/edit/{id}")
    public String editPatrouilleur(@PathVariable int id,
                                   @ModelAttribute Patrouilleurs patrouilleur,
                                   @RequestParam(required = false) String login,
                                   @RequestParam(required = false) String nouveauMotDePasse,
                                   RedirectAttributes redirectAttributes) {
        try {
            Patrouilleurs existing = patrouilleurService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Patrouilleur non trouvé"));

            existing.setNom(patrouilleur.getNom());
            existing.setRole(patrouilleur.getRole());
            existing.setTelephone(patrouilleur.getTelephone());
            existing.setEmail(patrouilleur.getEmail());
            existing.setDateRecrutement(patrouilleur.getDateRecrutement());
            if (patrouilleur.getSite() != null) {
                existing.setSite(patrouilleur.getSite());
            }

            String loginValue = login != null ? login.trim() : "";
            String motDePasseValue = nouveauMotDePasse != null ? nouveauMotDePasse.trim() : "";

            if (!loginValue.isEmpty() || !motDePasseValue.isEmpty()) {
                UserApp userApp = userAppService.findByPatrouilleurId(id)
                        .orElseThrow(() -> new RuntimeException("Aucun compte application lié à cet agent"));

                if (!loginValue.isEmpty()) {
                    userApp.setLogin(loginValue);
                }
                if (!motDePasseValue.isEmpty()) {
                    userApp.setMotDePasse(motDePasseValue);
                }

                userAppService.save(userApp);
            }

            patrouilleurService.save(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Agent modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification: " + e.getMessage());
        }
        return "redirect:/patrouilleurs/list";
    }

    // Supprimer un patrouilleur
    @GetMapping("/delete/{id}")
    public String deletePatrouilleur(@PathVariable int id) {
        patrouilleurService.deleteById(id);
        return "redirect:/patrouilleurs/list";
    }
}
