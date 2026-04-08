package com.example.serveur.repository;

import com.example.serveur.model.HistoriqueMessageStatus;
import com.example.serveur.model.StatusMessage;
import com.example.serveur.model.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface HistoriqueMessageStatusRepository extends JpaRepository<HistoriqueMessageStatus, Integer> {
      @Query("SELECT h FROM HistoriqueMessageStatus h WHERE h.message.userApp.idUserApp = :idUserApp")
    List<HistoriqueMessageStatus> findByUserAppId(@Param("idUserApp") int idUserApp);

    // **
    //  * Trouve le dernier statut d'un message spécifique
    //  */
    Optional<HistoriqueMessageStatus> findTopByMessage_IdMessageOrderByDateChangementDesc(Integer idMessage);
    
    /**
     * Trouve tous les changements de statut pour un message, triés par date décroissante
     */
    List<HistoriqueMessageStatus> findByMessage_IdMessageOrderByDateChangementDesc(Integer idMessage);
    
    /**
     * Vérifie s'il existe déjà un historique récent avec le même statut pour éviter les doublons
     */
    @Query("SELECT COUNT(h) > 0 FROM HistoriqueMessageStatus h " +
           "WHERE h.message = :message " +
           "AND h.status = :status " +
           "AND h.dateChangement > :dateMin")
    boolean existsByMessageAndIdStatusAndDateChangementAfter(
            @Param("message") Message message, 
            @Param("status") StatusMessage status, 
            @Param("dateMin") LocalDateTime dateMin);
    
    /**
     * Trouve tous les historiques pour un statut donné
     */
    List<HistoriqueMessageStatus> findByStatus_IdStatusMessage(Integer idStatusMessage);
    
    /**
     * Trouve tous les historiques dans une période donnée
     */
    @Query("SELECT h FROM HistoriqueMessageStatus h " +
           "WHERE h.dateChangement BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY h.dateChangement DESC")
    List<HistoriqueMessageStatus> findByDateChangementBetween(
            @Param("dateDebut") LocalDateTime dateDebut, 
            @Param("dateFin") LocalDateTime dateFin);
    
    /**
     * Compte le nombre de changements de statut pour un message
     */
    @Query("SELECT COUNT(h) FROM HistoriqueMessageStatus h WHERE h.message.idMessage = :idMessage")
    Long countByMessageId(@Param("idMessage") Integer idMessage);

}