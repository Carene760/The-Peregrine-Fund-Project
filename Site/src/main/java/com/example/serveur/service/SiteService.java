package com.example.serveur.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.serveur.repository.PatrouilleursRepository;
import com.example.serveur.repository.SiteRepository;
import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.model.Site;

@Service
public class SiteService {
    
    private final PatrouilleursRepository patrouilleurRepository;
    private final SiteRepository siteRepository;

    
    public SiteService(PatrouilleursRepository patrouilleurRepository, SiteRepository siteRepository) {
        this.patrouilleurRepository = patrouilleurRepository;
        this.siteRepository = siteRepository;
    }

    public Site save(Site site) {
        return siteRepository.save(site);
    }

    public List<Site> findAll() {
        return siteRepository.findAll();
    }

    public Optional<Site> findById(int id) {
        return siteRepository.findById(id);
    }

    public void deleteById(int id) {
        siteRepository.deleteById(id);
    }
    
    /**
     * Détermine l'ID du site basé sur le numéro de téléphone du patrouilleur
     */
    public Integer determinerIdSite(String phoneNumber) {
        try {
            // Nettoyer le numéro de téléphone (enlever les espaces, etc.)
            // String numeroNettoye = nettoyerNumero(phoneNumber);
            
            // Chercher le patrouilleur par numéro de téléphone
            Integer patrouilleurOpt = patrouilleurRepository.findIdSiteByTelephone(phoneNumber);
            
            if (patrouilleurOpt != null) {
                // Patrouilleurs patrouilleur = patrouilleurOpt.get();
                System.out.println("✅ Site trouvé: " + patrouilleurOpt + 
                                 " pour le numéro: " + phoneNumber);
                return patrouilleurOpt;
            } else {
                System.err.println("❌ Aucun patrouilleur trouvé pour le numéro: " + phoneNumber);
                return null; // ou une valeur par défaut si nécessaire
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la détermination du site: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Nettoie le numéro de téléphone pour la recherche
     */
    private String nettoyerNumero(String phoneNumber) {
        if (phoneNumber == null) return "";
        
        // Enlever les espaces, parenthèses, tirets, etc.
        return phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");
    }
    
    /**
     * Version alternative avec gestion des erreurs plus avancée
     */
    public Integer determinerIdSiteAvecFallback(String phoneNumber, Integer idSiteParDefaut) {
        Integer idSite = determinerIdSite(phoneNumber);
        
        if (idSite == null) {
            System.out.println("⚠️  Utilisation du site par défaut: " + idSiteParDefaut);
            return idSiteParDefaut;
        }
        
        return idSite;
    }
}