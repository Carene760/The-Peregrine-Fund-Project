package com.example.serveur.service;

import com.example.serveur.model.TypeAlerte;
import com.example.serveur.repository.TypeAlerteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeAlerteService {

    private final TypeAlerteRepository typeAlerteRepository;

    public TypeAlerteService(TypeAlerteRepository typeAlerteRepository) {
        this.typeAlerteRepository = typeAlerteRepository;
    }

    public TypeAlerte save(TypeAlerte typeAlerte) {
        return typeAlerteRepository.save(typeAlerte);
    }

    public List<TypeAlerte> findAll() {
        return typeAlerteRepository.findAll();
    }

    public Optional<TypeAlerte> findById(int id) {
        return typeAlerteRepository.findById(id);
    }

    public void deleteById(int id) {
        typeAlerteRepository.deleteById(id);
    }
}
