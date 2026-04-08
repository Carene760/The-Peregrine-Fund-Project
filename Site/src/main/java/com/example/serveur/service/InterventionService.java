package com.example.serveur.service;

import com.example.serveur.model.Intervention;
import com.example.serveur.repository.InterventionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;

    public InterventionService(InterventionRepository interventionRepository) {
        this.interventionRepository = interventionRepository;
    }

    public Intervention save(Intervention intervention) {
        return interventionRepository.save(intervention);
    }

    public List<Intervention> findAll() {
        return interventionRepository.findAll();
    }

    public Optional<Intervention> findById(int id) {
        return interventionRepository.findById(id);
    }

    public void deleteById(int id) {
        interventionRepository.deleteById(id);
    }
}
