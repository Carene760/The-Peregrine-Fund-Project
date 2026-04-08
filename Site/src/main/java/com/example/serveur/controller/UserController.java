package com.example.serveur.controller;

import com.example.serveur.model.*;
import com.example.serveur.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FonctionService fonctionService;
    private final SiteService siteService;
    private final PatrouilleurService patrouilleurService;

    @Autowired
    public UserController(UserService userService,
                        FonctionService fonctionService,
                        SiteService siteService,
                        PatrouilleurService patrouilleurService) {
        this.userService = userService;
        this.fonctionService = fonctionService;
        this.siteService = siteService;
        this.patrouilleurService = patrouilleurService;
    }

    @GetMapping
    public String showUser(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Fonction> fonctions = fonctionService.findAll();
        model.addAttribute("currentUser", currentUser);
        
        // Si l'utilisateur n'est pas déjà dans le modèle, l'ajouter
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        
        model.addAttribute("fonctions", fonctions);
        return "User"; // User.html
    }

    @GetMapping("/sites")
    @ResponseBody
    public List<Site> getSites() {
        return siteService.findAll(); // Retourne tous les sites
    }

    @PostMapping("/save")
    public String saveUser(
            @ModelAttribute User user,
            @RequestParam(required = false) String siteId,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DEBUG START ===");
        System.out.println("Nom: " + user.getNom());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Fonction: " + (user.getFonction() != null ? user.getFonction().getFonction() : "null"));
        System.out.println("Mot de passe reçu: " + user.getMotDePasse());
        System.out.println("Site ID reçu: " + siteId);

        try {
            String fonctionNom = user.getFonction().getFonction();

            // ==========================
            // CAS RESPONSABLE
            // ==========================
            if ("Responsable".equalsIgnoreCase(fonctionNom)) {
                System.out.println("TRAITEMENT RESPONSABLE");
                
                // Site est obligatoire pour Responsable
                if (siteId == null || siteId.isEmpty() || "undefined".equals(siteId) || "null".equals(siteId)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Le site est obligatoire pour un Responsable.");
                    redirectAttributes.addFlashAttribute("user", user);
                    return "redirect:/users";
                }
                
                Integer siteIdInt;
                try {
                    siteIdInt = Integer.parseInt(siteId.trim());
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Site ID invalide.");
                    redirectAttributes.addFlashAttribute("user", user);
                    return "redirect:/users";
                }
                
                // Validation des champs communs
                String errorMessage = validateUserCommonFields(user);
                if (errorMessage != null) {
                    redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                    redirectAttributes.addFlashAttribute("user", user);
                    return "redirect:/users";
                }
                
                // Sauvegarde de l'utilisateur (avec mot de passe par défaut déjà défini dans le formulaire)
                userService.save(user);
                
                // Création d'un Patrouilleur pour Responsable
                Patrouilleurs p = new Patrouilleurs();
                p.setNom(user.getNom());
                p.setTelephone(user.getTelephone());
                p.setRole("Responsable");
                p.setSite(siteService.findById(siteIdInt)
                        .orElseThrow(() -> new RuntimeException("Site invalide")));

                patrouilleurService.save(p);

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Responsable ajouté avec succès");

                return "redirect:/users";
            }

            // ==========================
            // CAS UTILISATEUR NORMAL
            // ==========================
            System.out.println("TRAITEMENT UTILISATEUR NORMAL");
            
            // Validation pour utilisateur normal
            String errorMessage = validateUser(user);
            if (errorMessage != null) {
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                redirectAttributes.addFlashAttribute("user", user);
                return "redirect:/users";
            }

            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur ajouté avec succès");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
        }
        
        System.out.println("=== DEBUG END ===");
        return "redirect:/users";
    }

    // Méthode de validation pour tous les utilisateurs (champs communs)
    private String validateUserCommonFields(User user) {
        if (user.getNom() == null || user.getNom().trim().isEmpty()) {
            return "Le nom est obligatoire";
        }
        if (user.getTelephone() == null || user.getTelephone().trim().isEmpty()) {
            return "Le téléphone est obligatoire";
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "L'email est obligatoire";
        }
        if (user.getAdresse() == null || user.getAdresse().trim().isEmpty()) {
            return "L'adresse est obligatoire";
        }
        
        return null;
    }

    // Méthode de validation pour utilisateurs normaux
    private String validateUser(User user) {
        // Validation des champs communs
        String commonError = validateUserCommonFields(user);
        if (commonError != null) {
            return commonError;
        }
        
        // Vérification du mot de passe pour les utilisateurs normaux
        if (user.getMotDePasse() == null || user.getMotDePasse().trim().isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        
        return null;
    }
    
    // Validation simple d'email
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    // Liste de tous les utilisateurs de l'application
    @GetMapping("list")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users-app/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        return "users-app/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un utilisateur
    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/users-app";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        model.addAttribute("user", user);
        return "users-app/edit"; // Vue Thymeleaf à créer
    }

    // Supprimer un utilisateur
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteById(id);
        return "redirect:/users-app";
    }
}