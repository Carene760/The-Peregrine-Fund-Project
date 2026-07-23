package com.example.theperegrinefund.security;

import android.util.Base64;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES/GCM/NoPadding encryption, matching the server's EncryptionUtil.
 *
 * SECURITY FIX: this used to be Cipher.getInstance("AES") (defaults to
 * AES/ECB/PKCS5Padding - not semantically secure, no IV). GCM uses a fresh
 * random 12-byte IV per message (prepended to the ciphertext) and a 128-bit
 * authentication tag. WIRE FORMAT CHANGE: incompatible with anything
 * encrypted by the old code; server and app must be updated together.
 */
public class CryptoUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final String description;

    public CryptoUtils(String description) {
        this.description = description;
    }

    // Méthode pour chiffrer
    public String chiffrer(String cleSecrete) throws Exception {
        return encrypt(cleSecrete, description);
    }

    // Méthode pour déchiffrer
    public static String dechiffrer(String cleSecrete, String texteChiffre) throws Exception {
        return decrypt(cleSecrete, texteChiffre);
    }

    public static String encrypt(String cleSecrete, String plainText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(cleSecrete.getBytes("UTF-8"), KEY_ALGORITHM);

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

        byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
        System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);

        // NO_WRAP: avoid embedded newlines that could corrupt SMS/HTTP payloads.
        return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP);
    }

    public static String decrypt(String cleSecrete, String texteChiffre) throws Exception {
        SecretKeySpec key = new SecretKeySpec(cleSecrete.getBytes("UTF-8"), KEY_ALGORITHM);
        byte[] decoded = Base64.decode(texteChiffre, Base64.DEFAULT);

        if (decoded.length < GCM_IV_LENGTH_BYTES) {
            throw new Exception("Ciphertext trop court pour contenir un IV GCM (" + decoded.length + " octets)");
        }

        byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH_BYTES);
        byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH_BYTES, decoded.length);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] decrypted = cipher.doFinal(cipherText);
        return new String(decrypted, "UTF-8");
    }
}
