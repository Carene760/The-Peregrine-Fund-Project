package com.example.serveur.repository;

import com.example.serveur.model.StatusMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusMessageRepository extends JpaRepository<StatusMessage, Integer> {
    @Query("SELECT s FROM StatusMessage s WHERE s.status = :status")
    StatusMessage findByStatus(@Param("status") String status);

    StatusMessage findFirstByStatusIgnoreCase(String status);

        // Vérifie si un status existe déjà
    boolean existsByStatus(String status);
}