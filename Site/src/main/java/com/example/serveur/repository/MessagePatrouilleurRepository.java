package com.example.serveur.repository;

import com.example.serveur.model.MessagePatrouilleur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessagePatrouilleurRepository extends JpaRepository<MessagePatrouilleur, Integer> {
    // Ajoutez ici des méthodes personnalisées si besoin
}