package com.example.serveur.service;

import com.example.serveur.model.StatusAgent;
import com.example.serveur.repository.StatusAgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusAgentService {

    private final StatusAgentRepository statusAgentRepository;

    public StatusAgentService(StatusAgentRepository statusAgentRepository) {
        this.statusAgentRepository = statusAgentRepository;
    }

    public StatusAgent save(StatusAgent statusAgent) {
        return statusAgentRepository.save(statusAgent);
    }

    public List<StatusAgent> findAll() {
        return statusAgentRepository.findAll();
    }

    public Optional<StatusAgent> findById(int id) {
        return statusAgentRepository.findById(id);
    }

    public void deleteById(int id) {
        statusAgentRepository.deleteById(id);
    }
}
