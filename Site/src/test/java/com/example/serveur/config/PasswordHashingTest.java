package com.example.serveur.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Targeted tests for P0-1 (BCrypt password hashing): verifies the exact
 * encoder bean type used across LoginController/SmsProcessingService/
 * UserController/UserAppService/PatrouilleurController behaves correctly,
 * and that the BCrypt-format detection regex used by
 * PasswordMigrationRunner correctly distinguishes hashed vs. plaintext
 * values (so the migration does not re-hash an already-hashed password, and
 * does not skip a plaintext one).
 */
class PasswordHashingTest {

    // Mirrors PasswordMigrationRunner.BCRYPT_PATTERN.
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    @Test
    void motDePasseCorrect_estAccepteParLEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("MonMotDePasse123");

        assertTrue(encoder.matches("MonMotDePasse123", hash));
    }

    @Test
    void motDePasseIncorrect_estRejeteParLEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("MonMotDePasse123");

        assertFalse(encoder.matches("MauvaisMotDePasse", hash));
    }

    @Test
    void hashBcrypt_estReconnuCommeDejaHashe() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("test");

        assertTrue(BCRYPT_PATTERN.matcher(hash).matches());
    }

    @Test
    void motDePasseEnClair_nEstPasReconnuCommeHashe() {
        assertFalse(BCRYPT_PATTERN.matcher("motdepasse123").matches());
        assertFalse(BCRYPT_PATTERN.matcher("password1+261382318042").matches());
    }
}
