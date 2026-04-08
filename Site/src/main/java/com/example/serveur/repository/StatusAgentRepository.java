package com.example.serveur.repository;

import com.example.serveur.model.StatusAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusAgentRepository extends JpaRepository<StatusAgent, Integer> {
    // Ajoutez ici des méthodes
}