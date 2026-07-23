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
            String url = ensureTrailingSlash(props.getProperty("server.url"));
            Log.d("ConfigLoader", "URL chargée: " + url);
            return url;
        } catch (Exception e) {
            Log.e("ConfigLoader", "Erreur de lecture du fichier config", e);
            throw e;
        }
    }

    /**
     * Retrofit's Retrofit.Builder#baseUrl() throws IllegalArgumentException
     * if the URL does not end with "/". config.properties values are
     * hand-edited (e.g. "http://192.168.1.103:8080") and easy to get wrong,
     * so every caller of getServerUrl()/getBackupServerUrl() goes through
     * this instead of trusting the raw property value.
     */
    private static String ensureTrailingSlash(String url) {
        if (url == null || url.isEmpty() || url.endsWith("/")) {
            return url;
        }
        return url + "/";
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
            // Pas de ensureTrailingSlash ici volontairement: contrairement à
            // getServerUrl() (consommé par Retrofit.Builder#baseUrl(), qui
            // EXIGE un "/" final), server.backup.url est consommé par
            // SyncService via une concaténation brute du type BASE_URL +
            // "/status" - un "/" final ajouté ici produirait un double
            // slash (".../sync//status") qui ne correspond à aucune route
            // côté serveur (@RequestMapping("/sync") + @GetMapping("/status")).
            String url = props.getProperty("server.backup.url");
            Log.d("ConfigLoader", "URL backup chargée: " + url);
            return url;
        } catch (Exception e) {
            Log.e("ConfigLoader", "Erreur de lecture du fichier config (backup)", e);
            throw e;
        }
    }
}

