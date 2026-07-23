package com.example.serveur.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Targeted tests for the AES/GCM migration (P2-9): round-trip correctness,
 * random-IV semantic security (no two ciphertexts for the same plaintext
 * are identical), and tamper detection (GCM's authentication tag must
 * reject a modified ciphertext instead of silently returning garbage).
 */
class EncryptionUtilTest {

    private static final String KEY = "75dEb3EMWpH5xvgTpTMGBpeD2eOpVMjF"; // 32 chars => AES-256

    @Test
    void chiffrerPuisDechiffrer_rendLeTexteOriginal() throws Exception {
        EncryptionUtil util = new EncryptionUtil(KEY);
        String original = "dateSignalement=2026-01-01T10:00:00/idIntervention=3/idUserApp=7/idStatus=1";

        String chiffre = util.chiffrer(original);
        String dechiffre = util.dechiffrer(chiffre);

        assertEquals(original, dechiffre);
    }

    @Test
    void deuxChiffrementsDuMemeTexte_produisentDesCiphertextsDifferents() throws Exception {
        // Regression guard for the old ECB bug: identical plaintext must NOT
        // produce identical ciphertext (random IV per call).
        EncryptionUtil util = new EncryptionUtil(KEY);
        String original = "login=agent1/password=secret";

        String chiffre1 = util.chiffrer(original);
        String chiffre2 = util.chiffrer(original);

        assertNotEquals(chiffre1, chiffre2);
        assertEquals(original, util.dechiffrer(chiffre1));
        assertEquals(original, util.dechiffrer(chiffre2));
    }

    @Test
    void ciphertextContientAuMoinsIvEtTagGcm() throws Exception {
        EncryptionUtil util = new EncryptionUtil(KEY);
        String chiffre = util.chiffrer("x");

        byte[] decoded = Base64.getDecoder().decode(chiffre);
        // 12 octets IV + 16 octets tag GCM minimum, quelle que soit la taille du clair.
        assertTrue(decoded.length >= 28);
    }

    @Test
    void ciphertextAltere_estRejeteParLeTagDauthentification() throws Exception {
        EncryptionUtil util = new EncryptionUtil(KEY);
        String chiffre = util.chiffrer("message sensible");

        byte[] decoded = Base64.getDecoder().decode(chiffre);
        decoded[decoded.length - 1] ^= 0x01; // altère le dernier octet (dans le tag/ciphertext)
        String altered = Base64.getEncoder().encodeToString(decoded);

        assertThrows(Exception.class, () -> util.dechiffrer(altered));
    }
}
