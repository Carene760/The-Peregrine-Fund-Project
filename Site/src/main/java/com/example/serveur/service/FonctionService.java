package com.example.serveur.service;

import com.example.serveur.model.Fonction;
import com.example.serveur.repository.FonctionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FonctionService {

    private final FonctionRepository fonctionRepository;

    public FonctionService(FonctionRepository fonctionRepository) {
        this.fonctionRepository = fonctionRepository;
    }

    public Fonction save(Fonction fonction) {
        return fonctionRepository.save(fonction);
    }

    public List<Fonction> findAll() {
        return fonctionRepository.findAll();
    }

    public Optional<Fonction> findById(int id) {
        return fonctionRepository.findById(id);
    }

    public void deleteById(int id) {
        fonctionRepository.deleteById(id);
    }
}
