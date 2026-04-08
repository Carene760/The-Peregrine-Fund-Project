package com.example.serveur.repository;

import com.example.serveur.model.Fonction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FonctionRepository extends JpaRepository<Fonction, Integer> {
    // Ajoutez ici des méthodes personnalisées si besoin
}
