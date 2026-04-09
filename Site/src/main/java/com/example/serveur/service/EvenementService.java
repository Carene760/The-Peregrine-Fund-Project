package com.example.serveur.service;

import com.example.serveur.model.Evenement;
import com.example.serveur.repository.EvenementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EvenementService {

    private final EvenementRepository evenementRepository;

    public EvenementService(EvenementRepository evenementRepository) {
        this.evenementRepository = evenementRepository;
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
}
