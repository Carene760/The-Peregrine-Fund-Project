package com.example.serveur.service;

import com.example.serveur.model.MessagePatrouilleur;
import com.example.serveur.repository.MessagePatrouilleurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessagePatrouilleurService {

    private final MessagePatrouilleurRepository messagePatrouilleurRepository;

    public MessagePatrouilleurService(MessagePatrouilleurRepository messagePatrouilleurRepository) {
        this.messagePatrouilleurRepository = messagePatrouilleurRepository;
    }

    public MessagePatrouilleur save(MessagePatrouilleur messagePatrouilleur) {
        return messagePatrouilleurRepository.save(messagePatrouilleur);
    }

    public List<MessagePatrouilleur> findAll() {
        return messagePatrouilleurRepository.findAll();
    }

    public Optional<MessagePatrouilleur> findById(int id) {
        return messagePatrouilleurRepository.findById(id);
    }

    public void deleteById(int id) {
        messagePatrouilleurRepository.deleteById(id);
    }
}
