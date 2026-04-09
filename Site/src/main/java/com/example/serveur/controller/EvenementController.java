package com.example.serveur.controller;

import com.example.serveur.model.Evenement;
import com.example.serveur.model.Message;
import com.example.serveur.model.User;
import com.example.serveur.repository.AlerteRepository;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import com.example.serveur.service.EvenementService;
import com.example.serveur.service.HistoriqueMessageEditService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/evenements")
public class EvenementController {

    private final EvenementService evenementService;
    private final HistoriqueMessageEditService historiqueMessageEditService;
    private final HistoriqueMessageStatusRepository historiqueMessageStatusRepository;
    private final AlerteRepository alerteRepository;

    public EvenementController(EvenementService evenementService,
                               HistoriqueMessageEditService historiqueMessageEditService,
                               HistoriqueMessageStatusRepository historiqueMessageStatusRepository,
                               AlerteRepository alerteRepository) {
        this.evenementService = evenementService;
        this.historiqueMessageEditService = historiqueMessageEditService;
        this.historiqueMessageStatusRepository = historiqueMessageStatusRepository;
        this.alerteRepository = alerteRepository;
    }

    @GetMapping
    public String listEvenements(Model model,
                                 @RequestParam(value = "nom", required = false) String nom,
                                 @RequestParam(value = "date", required = false) String date,
                                 HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Evenement> evenements = evenementService.findAll();

        if (nom != null && !nom.isBlank()) {
            evenements = evenements.stream()
                    .filter(evenement -> evenement.getNom() != null && evenement.getNom().toLowerCase().contains(nom.toLowerCase()))
                    .toList();
        }

        if (date != null && !date.isBlank()) {
            evenements = evenements.stream()
                    .filter(evenement -> evenement.getDate() != null && date.equals(evenement.getDate().toString()))
                    .toList();
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("evenements", evenements);
        model.addAttribute("filterNom", nom);
        model.addAttribute("filterDate", date);
        return "evenements";
    }

    @GetMapping("/{id}")
    public String detailEvenement(@PathVariable int id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Evenement evenement = evenementService.findById(id).orElseThrow(() -> new RuntimeException("Evenement non trouve"));
        List<Message> messages = evenementService.findMessagesByEvenement(id);
        Map<Integer, Object> statusMessage = new HashMap<>();
        Map<Integer, Object> alerteMessage = new HashMap<>();

        messages.forEach(message -> {
            historiqueMessageStatusRepository.findTopByMessage_IdMessageOrderByDateChangementDesc(message.getIdMessage())
                .ifPresent(status -> statusMessage.put(message.getIdMessage(), status));
            alerteRepository.findTopByMessage_IdMessageOrderByIdAlerteDesc(message.getIdMessage())
                .ifPresent(alerte -> alerteMessage.put(message.getIdMessage(), List.of(alerte)));
        });

        model.addAttribute("user", currentUser);
        model.addAttribute("evenement", evenement);
        model.addAttribute("messages", messages);
        model.addAttribute("statusMessage", statusMessage);
        model.addAttribute("alerteMessage", alerteMessage);
        return "evenement-detail";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createEvenement(@RequestBody Map<String, Object> payload,
                                                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");

        if (!historiqueMessageEditService.isAdmin(currentUser)) {
            response.put("success", false);
            response.put("message", "Action reservee a l administrateur");
            return ResponseEntity.status(403).body(response);
        }

        String nom = payload.get("nom") != null ? String.valueOf(payload.get("nom")).trim() : "";
        String description = payload.get("description") != null ? String.valueOf(payload.get("description")).trim() : "";
        String dateStr = payload.get("date") != null ? String.valueOf(payload.get("date")).trim() : "";

        if (nom.isEmpty()) {
            response.put("success", false);
            response.put("message", "Le titre de l'evenement est obligatoire");
            return ResponseEntity.badRequest().body(response);
        }

        if (dateStr.isEmpty()) {
            response.put("success", false);
            response.put("message", "La date de l'evenement est obligatoire");
            return ResponseEntity.badRequest().body(response);
        }

        Object selectedMessagesObj = payload.get("messageIds");
        List<Integer> messageIds = List.of();
        if (selectedMessagesObj instanceof List<?> list) {
            messageIds = new java.util.ArrayList<>();
            for (Object value : list) {
                if (value == null) {
                    continue;
                }
                if (value instanceof Number number) {
                    messageIds.add(number.intValue());
                }
            }
        }

        if (messageIds.isEmpty()) {
            response.put("success", false);
            response.put("message", "Veuillez selectionner au moins un message");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            Evenement evenement = evenementService.createEvenementWithMessages(nom, date, description, messageIds);

            response.put("success", true);
            response.put("message", "Evenement cree avec succes");
            response.put("id", evenement.getIdEvenement());
            response.put("redirectUrl", "/evenements/" + evenement.getIdEvenement());
            return ResponseEntity.ok(response);
        } catch (Exception exception) {
            response.put("success", false);
            response.put("message", "Date invalide ou impossible a traiter");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
