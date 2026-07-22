package com.example.serveur.service;

import com.example.serveur.model.*;
import com.example.serveur.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserAppService {

    private final UserAppRepository userAppRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAppService(UserAppRepository userAppRepository, PasswordEncoder passwordEncoder) {
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserApp save(UserApp userApp) {
        return userAppRepository.save(userApp);
    }

    public List<UserApp> findAll() {
        return userAppRepository.findAll();
    }

    public Optional<UserApp> findById(int id) {
        return userAppRepository.findById(id);
    }

    public Optional<UserApp> findByPatrouilleurId(int patrouilleurId) {
        return userAppRepository.findByPatrouilleurId(patrouilleurId);
    }

    public void deleteById(int id) {
        userAppRepository.deleteById(id);
    }

    public UserApp createForPatrouilleur(Patrouilleurs patrouilleur, String login, String motDePasse) {
        boolean alreadyExists = userAppRepository.findAll().stream()
                .anyMatch(userApp -> userApp.getPatrouilleur() != null
                        && userApp.getPatrouilleur().getIdPatrouilleur() == patrouilleur.getIdPatrouilleur());

        if (alreadyExists) {
            throw new IllegalStateException("Ce patrouilleur a déjà un compte.");
        }

        UserApp userApp = new UserApp();
        userApp.setLogin(login);
        userApp.setMotDePasse(passwordEncoder.encode(motDePasse));
        userApp.setPatrouilleur(patrouilleur);
        return userApp;
    }

    public List<UserApp> GenererUser(int nombreAgent, PatrouilleurService patrouilleurService) {
        List<UserApp> usersapp = new ArrayList<>();
        List<Patrouilleurs> patrouilleurs = patrouilleurService.findWithoutUserApp();

        for( int i= 0, created = 0; i < patrouilleurs.size() && created < nombreAgent; i++) {
            Patrouilleurs patrouilleur = patrouilleurs.get(i);
            UserApp userApp = new UserApp();
            userApp.setLogin("agent" + patrouilleur.getIdPatrouilleur());
            String generatedPassword = "password" + i + patrouilleur.getTelephone() + "" + i;
            userApp.setMotDePasse(passwordEncoder.encode(generatedPassword)); // hashé en BCrypt avant stockage
            userApp.setPatrouilleur(patrouilleur);
            usersapp.add(userApp);

            created++;
        }
        return usersapp;
    }
}
