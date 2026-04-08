package com.example.serveur.service;

import com.example.serveur.repository.PatrouilleursRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AllowedNumbersService {
    
    private final PatrouilleursRepository patrouilleurRepository;
    private Set<String> numerosAutorises;
    
    public AllowedNumbersService(PatrouilleursRepository patrouilleurRepository) {
        this.patrouilleurRepository = patrouilleurRepository;
        this.numerosAutorises = new HashSet<>();
    }
    
    @PostConstruct
    public void init() {
        refreshAllowedNumbers();
        System.out.println("✅ Service de numéros autorisés initialisé avec " + numerosAutorises.size() + " numéros");
    }
    
    /**
     * Rafraîchit la liste des numéros autorisés depuis la base de données
     */
    @Scheduled(fixedRate = 300000) // Rafraîchit toutes les 5 minutes (300000 ms)
    public void refreshAllowedNumbers() {
        try {
            List<String> telephonesAgents = patrouilleurRepository.findTelephonesByRoleAgent();
            Set<String> nouveauxNumeros = new HashSet<>(telephonesAgents);
            
            this.numerosAutorises = nouveauxNumeros;
            System.out.println("🔄 Liste des numéros autorisés rafraîchie: " + numerosAutorises.size() + " numéros");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rafraîchissement des numéros autorisés: " + e.getMessage());
        }
    }
    
    public Set<String> getNumerosAutorises() {
        return new HashSet<>(numerosAutorises); // Retourne une copie pour éviter les modifications externes
    }
    
    public boolean isNumeroAutorise(String numero) {
        return numerosAutorises.contains(numero);
    }
}