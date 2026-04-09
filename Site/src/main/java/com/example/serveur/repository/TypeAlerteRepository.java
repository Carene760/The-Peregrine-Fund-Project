package com.example.serveur.repository;

import com.example.serveur.model.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeAlerteRepository extends JpaRepository<TypeAlerte, Integer> {
    @Query("SELECT t FROM TypeAlerte t WHERE t.zone = :zone")
    TypeAlerte findByZone(@Param("zone") String zone);
}