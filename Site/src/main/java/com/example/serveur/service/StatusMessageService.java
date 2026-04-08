package com.example.serveur.service;

import com.example.serveur.model.StatusMessage;
import com.example.serveur.repository.StatusMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusMessageService {

    private final StatusMessageRepository statusMessageRepository;

    public StatusMessageService(StatusMessageRepository statusMessageRepository) {
        this.statusMessageRepository = statusMessageRepository;
    }

    public StatusMessage save(StatusMessage statusMessage) {
        return statusMessageRepository.save(statusMessage);
    }

    public List<StatusMessage> findAll() {
        return statusMessageRepository.findAll();
    }

    public Optional<StatusMessage> findById(int id) {
        return statusMessageRepository.findById(id);
    }

    public void deleteById(int id) {
        statusMessageRepository.deleteById(id);
    }
}
