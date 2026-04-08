package com.example.serveur.repository;

import com.example.serveur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);

    // Trouver par nom
    Optional<User> findByNom(String nom);

    // Trouver les utilisateurs par liste d'IDs de fonction
    @Query("SELECT u FROM User u WHERE u.fonction.idFonction IN :idFonctions")
    List<User> findByFonctionIdIn(@Param("idFonctions") List<Integer> idFonctions);

    @Query("SELECT u.email " +
       "FROM User u " +
       "JOIN u.fonction f " +
       "JOIN f.fonctionZoneAlertes fza " +
       "JOIN fza.typeAlerte ta " +
       "WHERE ta.zone = :zone")
    List<String> findEmailsByZoneAlerte(@Param("zone") String zone);
}
