package com.example.serveur.repository;

import com.example.serveur.model.Message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
     @Query("SELECT COUNT(m) > 0 FROM Message m WHERE " +
           "m.longitude = :longitude AND " +
           "m.latitude = :latitude ")
    boolean existsByLongitudeAndLatitude(
        @Param("longitude") Double longitude,
        @Param("latitude") Double latitude);

       // Compter les messages par site (nom du site + total)
       @Query(value = "SELECT s.Nom, COUNT(m.*)\r\n" + //
                            "FROM message m \r\n" + //
                            "JOIN UserApp u ON m.iduserapp = u.iduserapp\r\n" + //
                            "JOIN Patrouilleurs p ON u.id_patrouilleur = p.id_patrouilleur\r\n" + //
                            "JOIN Site s ON p.id_site = s.id_site\r\n" + //
                            "GROUP BY s.id_site, s.Nom\r\n" + //
                            "ORDER BY s.Nom", nativeQuery = true)
    List<Object[]> countMessagesBySite();

     boolean existsByDateSignalement(LocalDateTime dateSignalement);

  List<Message> findByUserApp_IdUserApp(int id);
    // NOUVELLE REQUÊTE : Récupérer tous les messages pour un site spécifique
    @Query("SELECT m FROM Message m " +
           "JOIN m.userApp ua " +
           "JOIN ua.patrouilleur p " +
           "JOIN p.site s " +
           "WHERE s.id_Site = :siteId " +
           "ORDER BY m.dateSignalement DESC")
    List<Message> findMessagesBySiteId(@Param("siteId") int siteId);
    
    // NOUVELLE REQUÊTE : Récupérer seulement les messages avec coordonnées pour un site
    @Query("SELECT m FROM Message m " +
           "JOIN m.userApp ua " +
           "JOIN ua.patrouilleur p " +
           "JOIN p.site s " +
           "WHERE s.id_Site = :siteId " +
           "AND m.latitude IS NOT NULL " +
           "AND m.longitude IS NOT NULL " +
           "ORDER BY m.dateSignalement DESC")
    List<Message> findMessagesBySiteIdWithCoordinates(@Param("siteId") int siteId);
    
    // OPTIONNEL : Compter les messages avec coordonnées par site
    @Query("SELECT s.Nom, COUNT(m) FROM Message m " +
           "JOIN m.userApp ua " +
           "JOIN ua.patrouilleur p " +
           "JOIN p.site s " +
           "WHERE m.latitude IS NOT NULL AND m.longitude IS NOT NULL " +
           "GROUP BY s.id_Site, s.Nom " +
           "ORDER BY s.Nom")
    List<Object[]> countMessagesWithCoordinatesBySite();

}