package com.example.serveur.controller;
import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.model.Intervention;
import com.example.serveur.model.Message;
import com.example.serveur.model.StatusMessage;
import com.example.serveur.model.Evenement;
import com.example.serveur.repository.MessageRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.serveur.repository.HistoriqueMessageStatusRepository;
import com.example.serveur.repository.InterventionRepository;
import com.example.serveur.repository.StatusMessageRepository;
import com.example.serveur.repository.EvenementRepository;


import java.util.List;


@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private MessageRepository messageRepository;

     @Autowired
    private HistoriqueMessageStatusRepository historiqueRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private StatusMessageRepository statusMessageRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    // 1. Upload : le téléphone envoie une liste de messages
    @PostMapping("/upload")
    public String uploadMessages(@RequestBody List<Message> messages) {
        for (Message msg : messages) {
            // Vérifier si le message existe déjà (via uuid ou date_signalement unique)
            if (!messageRepository.existsByDateSignalement(msg.getDateSignalement())) {
                messageRepository.save(msg);
            }
        }
        return "Upload terminé";
    }

    // 2. Download : le téléphone récupère les messages de son user
    @GetMapping("/download/{idUser}")
    public List<Message> downloadMessages(@PathVariable int idUser) {
        return messageRepository.findByUserApp_IdUserApp(idUser);
    }

    @GetMapping("/historique/{idUser}")
    public List<HistoriqueMessageStatus> getHistoriqueByUser(@PathVariable int idUser) {
        return historiqueRepository.findByUserAppId(idUser);
    }

    // === 2. Télécharger toutes les interventions disponibles ===
    @GetMapping("/interventions")
    public List<Intervention> getAllInterventions() {
        return interventionRepository.findAll();
    }

    // === 3. Télécharger tous les status disponibles ===
    @GetMapping("/status")
    public List<StatusMessage> getAllStatus() {
        return statusMessageRepository.findAll();
    }

    // === 4. Télécharger tous les evenements disponibles ===
    @GetMapping("/evenements")
    public List<Evenement> getAllEvenements() {
        return evenementRepository.findAll();
    }

}
