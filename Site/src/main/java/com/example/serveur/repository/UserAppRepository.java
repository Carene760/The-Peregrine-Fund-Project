package com.example.serveur.repository;

import com.example.serveur.model.UserApp;
import com.example.serveur.model.Patrouilleurs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Integer> {
    Optional<UserApp> findByLogin(String login);
    Optional<UserApp> findByLoginAndMotDePasse(String login, String motDePasse);
    @Query("select u from UserApp u where u.patrouilleur.idPatrouilleur = :patrouilleurId")
    Optional<UserApp> findByPatrouilleurId(int patrouilleurId);
    @Query("select u.idUserApp from UserApp u where u.login = :login")
    Optional<Integer> findIdByLogin(String login);

    Optional<UserApp> findFirstByPatrouilleur(Patrouilleurs patrouilleur);

    /**
     * Utilisé en secours quand le numéro de téléphone transmis au serveur
     * n'est pas celui du patrouilleur (cas de l'envoi HTTP direct depuis
     * l'appli, qui envoie le numéro fixe de la passerelle plutôt que celui
     * de l'agent connecté - voir SiteService.determinerIdSiteParUserApp).
     */
    @Query("select u.patrouilleur.site.id from UserApp u where u.idUserApp = :idUserApp")
    Optional<Integer> findIdSiteByUserAppId(@Param("idUserApp") Integer idUserApp);

}