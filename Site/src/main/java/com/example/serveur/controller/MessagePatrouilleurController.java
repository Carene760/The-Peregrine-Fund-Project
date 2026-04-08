package com.example.serveur.controller;

import com.example.serveur.model.MessagePatrouilleur;
import com.example.serveur.service.MessagePatrouilleurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/messages-patrouilleurs")
public class MessagePatrouilleurController {

    private final MessagePatrouilleurService messagePatrouilleurService;

    @Autowired
    public MessagePatrouilleurController(MessagePatrouilleurService messagePatrouilleurService) {
        this.messagePatrouilleurService = messagePatrouilleurService;
    }

    // Liste de tous les messages patrouilleurs
    @GetMapping
    public String listMessages(Model model) {
        List<MessagePatrouilleur> messages = messagePatrouilleurService.findAll();
        model.addAttribute("messages", messages);
        return "messages-patrouilleurs/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("message", new MessagePatrouilleur());
        return "messages-patrouilleurs/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un message
    @PostMapping("/add")
    public String addMessage(@ModelAttribute MessagePatrouilleur message) {
        messagePatrouilleurService.save(message);
        return "redirect:/messages-patrouilleurs";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        MessagePatrouilleur message = messagePatrouilleurService.findById(id)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));
        model.addAttribute("message", message);
        return "messages-patrouilleurs/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un message
    // @PostMapping("/edit/{id}")
    // public String editMessage(@PathVariable int id, @ModelAttribute MessagePatrouilleur message) {
    //     messagePatrouilleurService.save(messagePatrouilleurService.setId(message, id));
    //     return "redirect:/messages-patrouilleurs";
    // }

    // Supprimer un message
    @GetMapping("/delete/{id}")
    public String deleteMessage(@PathVariable int id) {
        messagePatrouilleurService.deleteById(id);
        return "redirect:/messages-patrouilleurs";
    }
}
