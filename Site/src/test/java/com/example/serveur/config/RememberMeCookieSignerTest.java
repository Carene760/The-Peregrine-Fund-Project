package com.example.serveur.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Targeted tests for the remember-me cookie forgery fix (P1-6): a signed
 * cookie must round-trip to the original email, and any tampering
 * (different email, corrupted signature, or a cookie forged with a
 * different secret entirely) must be rejected.
 */
class RememberMeCookieSignerTest {

    @Test
    void cookieValide_estAccepteEtRendLemailOriginal() {
        RememberMeCookieSigner signer = new RememberMeCookieSigner("test-secret-1");
        String cookieValue = signer.sign("user%40example.com");

        assertEquals("user%40example.com", signer.verifyAndExtractEmail(cookieValue));
    }

    @Test
    void cookieForge_sansSignatureValide_estRejete() {
        RememberMeCookieSigner signer = new RememberMeCookieSigner("test-secret-1");

        // Ancien comportement vulnérable: un client forgeait directement
        // "rememberUserEmail=<email>" sans aucune signature.
        assertNull(signer.verifyAndExtractEmail("admin%40example.com"));
    }

    @Test
    void cookieAvecEmailModifieApresSignature_estRejete() {
        RememberMeCookieSigner signer = new RememberMeCookieSigner("test-secret-1");
        String cookieValue = signer.sign("victim%40example.com");

        int lastDot = cookieValue.lastIndexOf('.');
        String signature = cookieValue.substring(lastDot);
        String tampered = "attacker%40example.com" + signature;

        assertNull(signer.verifyAndExtractEmail(tampered));
    }

    @Test
    void cookieSigneAvecUnAutreSecret_estRejete() {
        RememberMeCookieSigner signerA = new RememberMeCookieSigner("secret-A");
        RememberMeCookieSigner signerB = new RememberMeCookieSigner("secret-B");

        String cookieFromA = signerA.sign("user%40example.com");

        assertNull(signerB.verifyAndExtractEmail(cookieFromA));
    }
}
