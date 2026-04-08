package com.example.serveur.service;

import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.repository.PatrouilleursRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatrouilleurService {

    private final PatrouilleursRepository patrouilleursRepository;

    public PatrouilleurService(PatrouilleursRepository patrouilleursRepository) {
        this.patrouilleursRepository = patrouilleursRepository;
    }

    public Patrouilleurs save(Patrouilleurs patrouilleur) {
        return patrouilleursRepository.save(patrouilleur);
    }

    public List<Patrouilleurs> findAll() {
        return patrouilleursRepository.findAll();
    }

    public Optional<Patrouilleurs> findById(int id) {
        return patrouilleursRepository.findById(id);
    }

    public void deleteById(int id) {
        patrouilleursRepository.deleteById(id);
    }
}
