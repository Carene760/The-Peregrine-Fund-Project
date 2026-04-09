package com.example.theperegrinefund.security;

public class CredentialUtil {

    // Combine name et mot de passe avec un slash
    public static String combineNamePassword(String name, String password) {
        return "login=" + name + "/password=" + password;
    }

    // Sépare name et mot de passe
    public static String[] splitNamePassword(String combined) {
        if (combined == null) {
            return new String[]{"", ""};
        }

        String[] parts = combined.split("/", 2); // limite à 2 parties
        if (parts.length == 2 && parts[0].startsWith("login=") && parts[1].startsWith("password=")) {
            return new String[]{parts[0].substring("login=".length()), parts[1].substring("password=".length())};
        }

        // Compatibilité ancien format: name/password
        return parts;
    }

    
    
}
