package org.example.sansam.payment.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Locale;

public final class  IdempotencyKeyUtil {
    private IdempotencyKeyUtil() {

    }


    public static String forCancel(String paymentKey, long amount, String reason) {

        String normReason = Normalizer.normalize(reason, Normalizer.Form.NFKC);
        normReason = normReason.trim().replaceAll("\\s+"," ");
        normReason = normReason.toLowerCase(Locale.ROOT);

        // 충돌 줄이기 위해 구분자 명확히
        String canonical = String.join("|",
                "cancel",
                "pk=" + paymentKey,
                "amt=" + amount,
                "reason=" + normReason
        );

        return sha256Base64Url(canonical);
    }

    public static String sha256Base64Url(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(dig);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
