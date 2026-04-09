package com.example.serveur.repository;

import com.example.serveur.model.Alerte;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Integer> {
    // Compter le total des alertes
    long count();
    
    // Compter les alertes par site
    @Query(value = "SELECT id_site, COUNT(*) FROM alerte GROUP BY id_site", nativeQuery = true)
    List<Object[]> countAlertesBySite();
    
    // Compter les alertes par type (pour les pourcentages)
    @Query(value = "SELECT id_typealerte, COUNT(*) FROM alerte GROUP BY id_typealerte", nativeQuery = true)
    List<Object[]> countAlertesByType();

    Optional<Alerte> findTopByMessage_IdMessageOrderByIdAlerteDesc(Integer idMessage);
}