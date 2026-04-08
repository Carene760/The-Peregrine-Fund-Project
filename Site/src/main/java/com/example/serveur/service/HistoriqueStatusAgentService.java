package com.example.serveur.service;

import com.example.serveur.model.HistoriqueStatusAgent;
import com.example.serveur.repository.HistoriqueStatusAgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoriqueStatusAgentService {

    private final HistoriqueStatusAgentRepository historiqueStatusAgentRepository;

    public HistoriqueStatusAgentService(HistoriqueStatusAgentRepository historiqueStatusAgentRepository) {
        this.historiqueStatusAgentRepository = historiqueStatusAgentRepository;
    }

    public HistoriqueStatusAgent save(HistoriqueStatusAgent historiqueStatusAgent) {
        return historiqueStatusAgentRepository.save(historiqueStatusAgent);
    }

    public List<HistoriqueStatusAgent> findAll() {
        return historiqueStatusAgentRepository.findAll();
    }

    public Optional<HistoriqueStatusAgent> findById(int id) {
        return historiqueStatusAgentRepository.findById(id);
    }

    public void deleteById(int id) {
        historiqueStatusAgentRepository.deleteById(id);
    }
}
