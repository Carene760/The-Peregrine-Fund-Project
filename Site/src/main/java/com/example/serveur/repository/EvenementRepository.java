package com.example.serveur.repository;

import com.example.serveur.model.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Integer> {
    List<Evenement> findByDate(LocalDate date);
    List<Evenement> findByNomContainingIgnoreCase(String nom);
    Optional<Evenement> findFirstByNomIgnoreCase(String nom);
}
