package com.novapos.shared.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TotpUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TotpUtil() {
    }

    public static String generateSecret() {
        var bytes = new byte[20];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        try {
            var codeInt = Integer.parseInt(code);
            var now = System.currentTimeMillis() / 1000;
            return generateCode(secret, now / 30) == codeInt
                    || generateCode(secret, (now - 30) / 30) == codeInt;
        } catch (Exception e) {
            return false;
        }
    }

    private static int generateCode(String secret, long counter) throws NoSuchAlgorithmException, InvalidKeyException {
        var keyBytes = Base64.getDecoder().decode(secret);
        var mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        var counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }
        var hash = mac.doFinal(counterBytes);
        var offset = hash[hash.length - 1] & 0x0F;
        var binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return binary % 1_000_000;
    }
}
