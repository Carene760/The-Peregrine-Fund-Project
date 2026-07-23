package com.example.serveur.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES/GCM/NoPadding encryption, matching the Android app's CryptoUtils.
 *
 * SECURITY FIX: this used to be Cipher.getInstance("AES"), which on the JVM
 * defaults to AES/ECB/PKCS5Padding - ECB mode is not semantically secure
 * (identical plaintext blocks produce identical ciphertext blocks, and there
 * is no IV/nonce). GCM is an authenticated mode: each message is encrypted
 * with a fresh random 12-byte IV (prepended to the ciphertext, standard
 * practice since the IV is not secret) and a 128-bit auth tag that also
 * protects integrity (tampering is detected, not just silently decrypted).
 *
 * WIRE FORMAT CHANGE: ciphertext produced by the old ECB code cannot be
 * decrypted by this class (and vice versa). Both sides (server + Android
 * app) must be updated together - see CryptoUtils.java / Message.java on
 * the app side. Any already-stored ciphertext becomes unreadable; this is
 * acceptable because this is pre-production test data (see project audit).
 */
@Component
public class EncryptionUtil {

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    // Le constructeur reçoit la clé depuis la configuration
    public EncryptionUtil(@Value("${encryption.secret-key}") String secretKey) {
        this.keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
    }

    private boolean estBase64Valide(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        String cleaned = str.replaceAll("\\s+", "")
                .replace("-", "+")
                .replace("_", "/");

        if (cleaned.length() % 4 != 0) {
            cleaned = cleaned + "=".repeat((4 - cleaned.length() % 4) % 4);
        }

        try {
            Base64.getDecoder().decode(cleaned);
            return true;
        } catch (IllegalArgumentException e) {
            try {
                Base64.getUrlDecoder().decode(cleaned);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }

    public String chiffrer(String texteClair) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] cipherText = cipher.doFinal(texteClair.getBytes(StandardCharsets.UTF_8));

        byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
        System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(ivAndCipherText);
    }

    public String dechiffrer(String texteChiffre) throws Exception {
        try {
            String texteNettoye = texteChiffre.replaceAll("\\s+", "")
                    .replace("-", "+")
                    .replace("_", "/");

            if (texteNettoye.length() % 4 != 0) {
                texteNettoye = texteNettoye + "=".repeat((4 - texteNettoye.length() % 4) % 4);
            }

            if (!estBase64Valide(texteNettoye)) {
                throw new Exception("Le texte ne semble pas être au format Base64 valide: " + texteChiffre);
            }

            byte[] decoded = Base64.getDecoder().decode(texteNettoye);
            if (decoded.length < GCM_IV_LENGTH_BYTES) {
                throw new Exception("Ciphertext trop court pour contenir un IV GCM (" + decoded.length + " octets)");
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du déchiffrement: " + e.getMessage());
            System.err.println("❌ Texte à déchiffrer: '" + texteChiffre + "'");
            throw new Exception("Échec du déchiffrement: " + e.getMessage(), e);
        }
    }
}
