package com.example.serveur.controller;

import com.example.serveur.model.StatusMessage;
import com.example.serveur.service.StatusMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/status-messages")
public class StatusMessageController {

    private final StatusMessageService statusMessageService;

    @Autowired
    public StatusMessageController(StatusMessageService statusMessageService) {
        this.statusMessageService = statusMessageService;
    }

    // Liste de tous les status messages
    @GetMapping
    public String listStatusMessages(Model model) {
        List<StatusMessage> statusMessages = statusMessageService.findAll();
        model.addAttribute("statusMessages", statusMessages);
        return "status-messages/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("statusMessage", new StatusMessage());
        return "status-messages/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un status message
    @PostMapping("/add")
    public String addStatusMessage(@ModelAttribute StatusMessage statusMessage) {
        statusMessageService.save(statusMessage);
        return "redirect:/status-messages";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        StatusMessage statusMessage = statusMessageService.findById(id)
                .orElseThrow(() -> new RuntimeException("Status message non trouvé"));
        model.addAttribute("statusMessage", statusMessage);
        return "status-messages/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un status message
    // @PostMapping("/edit/{id}")
    // public String editStatusMessage(@PathVariable int id, @ModelAttribute StatusMessage statusMessage) {
    //     statusMessageService.save(statusMessageService.setId(statusMessage, id));
    //     return "redirect:/status-messages";
    // }

    // Supprimer un status message
    @GetMapping("/delete/{id}")
    public String deleteStatusMessage(@PathVariable int id) {
        statusMessageService.deleteById(id);
        return "redirect:/status-messages";
    }
}
