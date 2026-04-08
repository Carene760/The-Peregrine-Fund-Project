package com.example.serveur.controller;

import com.example.serveur.model.Message;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private MessageRepository messageRepository;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Liste de tous les messages
    @GetMapping
    public String listMessages(Model model) {
        List<Message> messages = messageService.findAll();
        model.addAttribute("messages", messages);
        return "messages/list"; // Vue Thymeleaf à créer
    }

    // Formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("message", new Message());
        return "messages/add"; // Vue Thymeleaf à créer
    }

    // Ajouter un message
    @PostMapping("/add")
    public String addMessage(@ModelAttribute Message message) {
        messageService.save(message);
        return "redirect:/messages";
    }

    // Formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Message message = messageService.findById(id)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));
        model.addAttribute("message", message);
        return "messages/edit"; // Vue Thymeleaf à créer
    }

    // Modifier un message
    // @PostMapping("/edit/{id}")
    // public String editMessage(@PathVariable int id, @ModelAttribute Message message) {
    //     messageService.save(messageService.setId(message, id));
    //     return "redirect:/messages";
    // }

    // Supprimer un message
    @GetMapping("/delete/{id}")
    public String deleteMessage(@PathVariable int id) {
        messageService.deleteById(id);
        return "redirect:/messages";
    }

    // Récupérer les messages d’un utilisateur précis
    @GetMapping("/{idUser}")
    public List<Message> getMessages(@PathVariable int idUser) {
        // On utilise une méthode personnalisée du repository
        return messageRepository.findByUserApp_IdUserApp(idUser);
    }
}
