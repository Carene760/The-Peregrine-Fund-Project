package com.example.theperegrinefund.security;

import android.content.Context;
import java.io.InputStream;
import java.util.Properties;
import android.util.Log;

public class ConfigLoader {

    public static String getSecretKey(Context context) throws Exception {
        Properties props = new Properties();
        try (InputStream input = context.getAssets().open("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier config.properties introuvable !");
            }
            props.load(input);
        }
        return props.getProperty("secret.key");
    }

    // Nouvelle méthode pour récupérer le numéro
    public static String getFixedNumber(Context context) throws Exception {
        Properties props = new Properties();
        try (InputStream input = context.getAssets().open("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier config.properties introuvable !");
            }
            props.load(input);
        }
        return props.getProperty("fixed.number");
    }

    // Nouvelle méthode pour récupérer l'adresse IP ou URL du serveur
    public static String getServerUrl(Context context) throws Exception {
        Properties props = new Properties();
        try (InputStream input = context.getAssets().open("config.properties")) {
            if (input == null) {
                Log.e("ConfigLoader", "Fichier config.properties introuvable !");
                throw new RuntimeException("Fichier config.properties introuvable !");
            }
            props.load(input);
            String url = props.getProperty("server.url");
            Log.d("ConfigLoader", "URL chargée: " + url);
            return url;
        } catch (Exception e) {
            Log.e("ConfigLoader", "Erreur de lecture du fichier config", e);
            throw e;
        }
    }
    /**
     * Cle partagee envoyee dans l'en-tete X-API-Key pour authentifier les
     * appels de l'app vers /api/** et /sync/** (voir ApiKeyInterceptor cote
     * serveur). Doit correspondre a app.api.key dans application.properties.
     */
    public static String getApiKey(Context context) throws Exception {
        Properties props = new Properties();
        try (InputStream input = context.getAssets().open("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier config.properties introuvable !");
            }
            props.load(input);
        }
        return props.getProperty("api.key");
    }

    public static String getBackupServerUrl(Context context) throws Exception {
        Properties props = new Properties();
        try (InputStream input = context.getAssets().open("config.properties")) {
            if (input == null) {
                Log.e("ConfigLoader", "Fichier config.properties introuvable !");
                throw new RuntimeException("Fichier config.properties introuvable !");
            }
            props.load(input);
            String url = props.getProperty("server.backup.url");
            Log.d("ConfigLoader", "URL backup chargée: " + url);
            return url;
        } catch (Exception e) {
            Log.e("ConfigLoader", "Erreur de lecture du fichier config (backup)", e);
            throw e;
        }
    }
}

