package com.example.serveur.repository;

import com.example.serveur.model.FonctionZoneAlerte;
import com.example.serveur.model.FonctionZoneAlerteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FonctionZoneAlerteRepository extends JpaRepository<FonctionZoneAlerte, FonctionZoneAlerteId> {
    
    // Trouver par zone d'alerte
    @Query("SELECT fza FROM FonctionZoneAlerte fza WHERE fza.typeAlerte.zone = :zone")
    List<FonctionZoneAlerte> findByTypeAlerteZone(@Param("zone") String zone);
    
    // Trouver par ID de type d'alerte
    @Query("SELECT fza FROM FonctionZoneAlerte fza WHERE fza.typeAlerte.idTypeAlerte = :idTypeAlerte")
    List<FonctionZoneAlerte> findByTypeAlerteId(@Param("idTypeAlerte") Integer idTypeAlerte);
}