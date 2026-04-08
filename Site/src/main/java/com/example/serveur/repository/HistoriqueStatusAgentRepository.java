package com.example.serveur.repository;

import com.example.serveur.model.HistoriqueStatusAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueStatusAgentRepository extends JpaRepository<HistoriqueStatusAgent, Integer> {
    // Ajoutez ici des méthodes personnalisées si
}