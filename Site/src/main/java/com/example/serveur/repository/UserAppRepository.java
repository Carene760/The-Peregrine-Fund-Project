package com.example.serveur.repository;

import com.example.serveur.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Integer> {
    Optional<UserApp> findByLogin(String login);
    Optional<UserApp> findByLoginAndMotDePasse(String login, String motDePasse);
    @Query("select u.idUserApp from UserApp u where u.login = :login")
    Optional<Integer> findIdByLogin(String login);

}