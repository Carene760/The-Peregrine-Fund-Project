package com.example.serveur.controller;

import com.example.serveur.model.*;
import com.example.serveur.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/users-app")
public class UserAppController {

    private final UserAppService userAppService;
    private final PatrouilleurService patrouilleurService;

    @Autowired
    public UserAppController(UserAppService userAppService, PatrouilleurService patrouilleurService) {
        this.userAppService = userAppService;
        this.patrouilleurService = patrouilleurService;
    }

    // Liste de tous les utilisateurs de l'application
   // Liste de tous les utilisateurs de l'application
    @GetMapping
    public String showUserApp(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("availablePatrouilleurs", patrouilleurService.findWithoutUserApp());
        return "agent"; // Vue Thymeleaf
    }

    // Ajouter des utilisateurs agents
    @PostMapping("/save")
    public String saveUserApp(@RequestParam(required = false) Integer patrouilleurId,
                              @RequestParam(required = false) String login,
                              @RequestParam(required = false) String motDePasse,
                              RedirectAttributes redirectAttributes) {

        if (patrouilleurId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner un patrouilleur.");
            return "redirect:/users-app";
        }

        if (login == null || login.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le login est obligatoire.");
            return "redirect:/users-app";
        }

        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe est obligatoire.");
            return "redirect:/users-app";
        }

        try {
            Patrouilleurs patrouilleur = patrouilleurService.findById(patrouilleurId)
                    .orElseThrow(() -> new RuntimeException("Patrouilleur introuvable"));

            UserApp userApp = userAppService.createForPatrouilleur(patrouilleur, login.trim(), motDePasse.trim());
            userAppService.save(userApp);
            redirectAttributes.addFlashAttribute("successMessage", "Compte créé avec succès pour " + patrouilleur.getNom() + ".");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de la création du compte: " + e.getMessage());
        }

        return "redirect:/users-app";
    }


    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("userApp", new UserApp());
        return "users-app/add"; // Vue Thymeleaf à créer
    }

    

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        UserApp userApp = userAppService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        model.addAttribute("userApp", userApp);
        return "users-app/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un utilisateur
    // @PostMapping("/edit/{id}")
    // public String editUser(@PathVariable int id, @ModelAttribute UserApp userApp) {
    //     userAppService.save(userAppService.setId(userApp, id));
    //     return "redirect:/users-app";
    // }

    // Supprimer un utilisateur
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userAppService.deleteById(id);
        return "redirect:/users-app";
    }
}
