package com.example.serveur.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Signs/verifies the "rememberUserEmail" cookie value with HMAC-SHA256 so it
 * cannot be forged.
 *
 * Previously the cookie only stored the URL-encoded email with no integrity
 * check: anyone who knew (or guessed) a valid user's email could set that
 * cookie themselves and be logged in as that user with zero password check
 * (see RememberUserInterceptor). The cookie now carries "email.signature",
 * and the interceptor only trusts it if the signature verifies.
 */
@Component
public class RememberMeCookieSigner {

    private final Mac mac;

    public RememberMeCookieSigner(@Value("${app.remember-me.secret}") String secret) {
        try {
            this.mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'initialiser la signature du cookie remember-me", e);
        }
    }

    /** Builds the full cookie value: "<email>.<base64-hmac-of-email>". */
    public synchronized String sign(String email) {
        byte[] signature = mac.doFinal(email.getBytes(StandardCharsets.UTF_8));
        return email + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }

    /** Returns the email if the signed cookie value is valid, or null otherwise. */
    public synchronized String verifyAndExtractEmail(String cookieValue) {
        if (cookieValue == null) {
            return null;
        }
        int lastDot = cookieValue.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == cookieValue.length() - 1) {
            return null;
        }
        String email = cookieValue.substring(0, lastDot);
        String providedSignature = cookieValue.substring(lastDot + 1);

        byte[] expectedSignature = mac.doFinal(email.getBytes(StandardCharsets.UTF_8));
        String expectedEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSignature);

        if (!MessageDigest.isEqual(
                expectedEncoded.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        return email;
    }
}
