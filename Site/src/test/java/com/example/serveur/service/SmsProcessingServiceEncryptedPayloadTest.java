package com.example.serveur.service;

import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Targeted regression test for P2-9: after switching AES to GCM, a real
 * ciphertext is no longer a multiple of 16 bytes (GCM has no block
 * alignment, unlike the old ECB mode). SmsProcessingService.processMessage
 * must still correctly recognise a GCM ciphertext as "encrypted" (and
 * decrypt it) instead of treating it as plaintext because of a leftover
 * ECB-era heuristic.
 */
class SmsProcessingServiceEncryptedPayloadTest {

    private static final String KEY = "75dEb3EMWpH5xvgTpTMGBpeD2eOpVMjF";

    @Test
    void messageChiffreEnGcm_estCorrectementDetecteEtDechiffre() throws Exception {
        EncryptionUtil encryptionUtil = new EncryptionUtil(KEY);
        UserAppRepository userAppRepository = Mockito.mock(UserAppRepository.class);
        SmsProcessingService service = new SmsProcessingService(
                encryptionUtil, userAppRepository, new BCryptPasswordEncoder(), "/");

        String original = "login=agent1/password=secret";
        String chiffre = encryptionUtil.chiffrer(original);

        String result = service.processMessage(chiffre, "+261382318042");

        assertEquals(original, result);
    }

    @Test
    void messageCourtNonChiffre_resteInchange() throws Exception {
        EncryptionUtil encryptionUtil = new EncryptionUtil(KEY);
        UserAppRepository userAppRepository = Mockito.mock(UserAppRepository.class);
        SmsProcessingService service = new SmsProcessingService(
                encryptionUtil, userAppRepository, new BCryptPasswordEncoder(), "/");

        String result = service.processMessage("OK", "+261382318042");

        assertEquals("OK", result);
    }
}
