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
        return "agent"; // Vue Thymeleaf
    }

    // Ajouter des utilisateurs agents
    @PostMapping("/save")
    public String saveUserApp(@RequestParam(required = false) Integer nombreAgent, 
                            RedirectAttributes redirectAttributes) {

        // Validation
        if (nombreAgent == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le nombre d'agents est requis");
            return "redirect:/users-app";
        }

        if (nombreAgent <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le nombre d'agents doit être supérieur à 0");
            redirectAttributes.addFlashAttribute("nombreAgentError", "Veuillez entrer un nombre valide (1-100)");
            return "redirect:/users-app";
        }

        if (nombreAgent > 100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le nombre d'agents ne peut pas dépasser 100");
            redirectAttributes.addFlashAttribute("nombreAgentError", "Veuillez entrer un nombre entre 1 et 100");
            return "redirect:/users-app";
        }

        try {
            List<UserApp> usersapp = userAppService.GenererUser(nombreAgent, patrouilleurService);

            if (usersapp.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Aucun agent n'a pu être créé car tous les agents sont déjà pris.");
            } else {
                usersapp.forEach(userAppService::save);
                String message = "";
                if(usersapp.size() == nombreAgent)
                {
                    message = usersapp.size() + " agent(s) ajouté(s) avec succès!";
                }
                else if(usersapp.size() < nombreAgent) 
                {
                    message = "Seul(s) " + usersapp.size() + " agent(s) ont(a) été ajouté(s) car le nombre d'agents disponibles est limité";
                }
                redirectAttributes.addFlashAttribute("successMessage", message);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de l'ajout des agents: " + e.getMessage());
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
