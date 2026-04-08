package com.example.serveur.service;

import com.example.serveur.model.*;
import com.example.serveur.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserAppService {

    private final UserAppRepository userAppRepository;

    public UserAppService(UserAppRepository userAppRepository) {
        this.userAppRepository = userAppRepository;
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

    public void deleteById(int id) {
        userAppRepository.deleteById(id);
    }

    public List<UserApp> GenererUser(int nombreAgent, PatrouilleurService patrouilleurService) {
        List<UserApp> usersapp = new ArrayList<>();
        List<Patrouilleurs> patrouilleurs = patrouilleurService.findAll();
        List<UserApp> existingUsers = userAppRepository.findAll();

        for( int i= 0, created = 0; i < patrouilleurs.size() && created < nombreAgent; i++) {
            Patrouilleurs patrouilleur = patrouilleurs.get(i);
            boolean alreadyExists = existingUsers.stream()
                    .anyMatch(u -> u.getPatrouilleur().getIdPatrouilleur() == patrouilleur.getIdPatrouilleur());
            if (alreadyExists) {
                continue;
            }
            UserApp userApp = new UserApp();
            userApp.setLogin("agent" + patrouilleur.getIdPatrouilleur());
            userApp.setMotDePasse("password" + i + patrouilleur.getTelephone() + "" + i ); // Vous devriez hasher les mots de passe dans une vraie application
            userApp.setPatrouilleur(patrouilleur);
            usersapp.add(userApp);

            created++;
        }
        return usersapp;
    }
}
