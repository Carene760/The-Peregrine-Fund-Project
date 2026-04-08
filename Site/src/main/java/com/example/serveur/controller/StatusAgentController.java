package com.example.serveur.controller;

import com.example.serveur.model.StatusAgent;
import com.example.serveur.service.StatusAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/status-agents")
public class StatusAgentController {

    private final StatusAgentService statusAgentService;

    @Autowired
    public StatusAgentController(StatusAgentService statusAgentService) {
        this.statusAgentService = statusAgentService;
    }

    // Liste de tous les status agents
    @GetMapping
    public String listStatusAgents(Model model) {
        List<StatusAgent> statusAgents = statusAgentService.findAll();
        model.addAttribute("statusAgents", statusAgents);
        return "status-agents/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("statusAgent", new StatusAgent());
        return "status-agents/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un status agent
    @PostMapping("/add")
    public String addStatusAgent(@ModelAttribute StatusAgent statusAgent) {
        statusAgentService.save(statusAgent);
        return "redirect:/status-agents";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        StatusAgent statusAgent = statusAgentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Status agent non trouvé"));
        model.addAttribute("statusAgent", statusAgent);
        return "status-agents/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un status agent
    // @PostMapping("/edit/{id}")
    // public String editStatusAgent(@PathVariable int id, @ModelAttribute StatusAgent statusAgent) {
    //     statusAgentService.save(statusAgentService.setId(statusAgent, id));
    //     return "redirect:/status-agents";
    // }

    // Supprimer un status agent
    @GetMapping("/delete/{id}")
    public String deleteStatusAgent(@PathVariable int id) {
        statusAgentService.deleteById(id);
        return "redirect:/status-agents";
    }
}
