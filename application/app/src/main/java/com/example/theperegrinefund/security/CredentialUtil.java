package com.example.theperegrinefund.security;

public class CredentialUtil {

    // Combine name et mot de passe avec un slash
    public static String combineNamePassword(String name, String password) {
        return name + "/" + password;
    }

    // Sépare name et mot de passe
    public static String[] splitNamePassword(String combined) {
        return combined.split("/", 2); // limite à 2 parties
    }

    
    
}
