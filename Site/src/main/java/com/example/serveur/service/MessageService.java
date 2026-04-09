package com.example.serveur.service;

import com.example.serveur.model.Message;
import com.example.serveur.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }
    
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
    
    public Message getMessageById(int id) {
        Optional<Message> msg = messageRepository.findById(id);
        return msg.orElse(null);
    }

    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    public Optional<Message> findById(int id) {
        return messageRepository.findById(id);
    }

    public void deleteById(int id) {
        messageRepository.deleteById(id);
    }

    public List<Object[]> countMessagesBySite() {
        return messageRepository.countMessagesBySite();
    }

   // NOUVELLE MÉTHODE : Récupérer tous les messages d'un site spécifique
    public List<Message> findMessagesBySite(int siteId) {
        return messageRepository.findMessagesBySiteId(siteId);
    }
    
    // NOUVELLE MÉTHODE : Récupérer les messages avec coordonnées pour un site
    public List<Message> findMessagesWithCoordinatesBySite(int siteId) {
        return messageRepository.findMessagesBySiteIdWithCoordinates(siteId);
    }

    public List<Message> findMessagesByEvenement(int evenementId) {
        return messageRepository.findByEvenementId(evenementId);
    }

    public List<Message> findMessagesByDate(LocalDate dateSignalement) {
        return messageRepository.findByDateSignalementDate(dateSignalement);
    }

    public int updateDescription(int messageId, String description) {
        return messageRepository.updateDescriptionById(messageId, description);
    }
    
}
