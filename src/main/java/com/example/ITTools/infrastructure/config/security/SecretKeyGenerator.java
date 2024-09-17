package com.example.ITTools.infrastructure.config.security;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    private static final int KEY_LENGTH = 32; // 256 bits

    public static String generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[KEY_LENGTH];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
