package com.example.serveur.service;

import com.example.serveur.model.Message;
import com.example.serveur.model.Evenement;
import com.example.serveur.repository.MessageRepository;
import com.example.serveur.repository.EvenementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final MessageRepository messageRepository;

    public EvenementService(EvenementRepository evenementRepository, MessageRepository messageRepository) {
        this.evenementRepository = evenementRepository;
        this.messageRepository = messageRepository;
    }

    public Evenement save(Evenement evenement) {
        return evenementRepository.save(evenement);
    }

    public List<Evenement> findAll() {
        return evenementRepository.findAll();
    }

    public Optional<Evenement> findById(int id) {
        return evenementRepository.findById(id);
    }

    public List<Evenement> findByDate(LocalDate date) {
        return evenementRepository.findByDate(date);
    }

    public List<Evenement> findByNom(String nom) {
        return evenementRepository.findByNomContainingIgnoreCase(nom);
    }

    public void deleteById(int id) {
        evenementRepository.deleteById(id);
    }

    @Transactional
    public Evenement createEvenementWithMessages(String nom, LocalDate date, String description, List<Integer> messageIds) {
        Evenement evenement = new Evenement();
        evenement.setNom(nom);
        evenement.setDate(date);
        evenement.setDescription(description);

        Evenement savedEvenement = evenementRepository.save(evenement);

        if (messageIds != null && !messageIds.isEmpty()) {
            List<Message> messages = messageRepository.findAllById(messageIds);
            for (Message message : messages) {
                message.setEvenement(savedEvenement);
            }
            messageRepository.saveAll(messages);
        }

        return savedEvenement;
    }

    public List<Message> findMessagesByEvenement(int evenementId) {
        return messageRepository.findByEvenementId(evenementId);
    }
}
