package com.example.theperegrinefund.security;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    // Attribut description, si c’est pour un objet particulier
    private String description;

    public CryptoUtils(String description) {
        this.description = description;
    }

    // Méthode pour chiffrer
    public String chiffrer(String cleSecrete) throws Exception {
        SecretKeySpec key = new SecretKeySpec(cleSecrete.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(description.getBytes("UTF-8"));
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    // Méthode pour déchiffrer
    public static String dechiffrer(String cleSecrete, String texteChiffre) throws Exception {
        SecretKeySpec key = new SecretKeySpec(cleSecrete.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.decode(texteChiffre, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, "UTF-8");
    }
}
