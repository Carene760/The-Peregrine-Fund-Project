package com.example.serveur.repository;

import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatrouilleursRepository extends JpaRepository<Patrouilleurs, Integer> {
    
    @Query("SELECT DISTINCT p.telephone FROM Patrouilleurs p WHERE p.role = 'Agent'")
    List<String> findTelephonesByRoleAgent();
    
    // Alternative avec paramètre
    @Query("SELECT DISTINCT p.telephone FROM Patrouilleurs p WHERE p.role = :role")
    List<String> findTelephonesByRole(@Param("role") String role);

    @Query("SELECT p.site.id FROM Patrouilleurs p WHERE p.telephone = :telephone")
    Integer findIdSiteByTelephone(@Param("telephone") String telephone);

    @Query("SELECT p FROM Patrouilleurs p WHERE LOWER(p.nom) = LOWER(:nom) AND LOWER(p.site.Nom) = LOWER(:siteNom)")
    Optional<Patrouilleurs> findFirstByNomAndSiteNomIgnoreCase(@Param("nom") String nom, @Param("siteNom") String siteNom);
    @Query("""
            SELECT p
            FROM Patrouilleurs p
            WHERE p.idPatrouilleur NOT IN (
                SELECT u.patrouilleur.idPatrouilleur
                FROM UserApp u
                WHERE u.patrouilleur IS NOT NULL
            )
            """)
    List<Patrouilleurs> findPatrouilleursWithoutUserApp();
    
    // @Query("SELECT p FROM Patrouilleurs p WHERE p.telephone = :telephone")
    // Optional<Patrouilleurs> findByTelephone( String telephone);
}