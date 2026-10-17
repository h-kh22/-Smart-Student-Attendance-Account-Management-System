package com.example.studentattendance.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final String FORMAT = "pbkdf2-sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] derivedKey = derive(password.toCharArray(), salt, ITERATIONS);
        return FORMAT + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivedKey);
    }

    public static boolean isEncoded(String value) {
        return value != null && value.startsWith(FORMAT + "$");
    }

    public static boolean verify(String password, String encodedHash) {
        if (password == null || !isEncoded(encodedHash)) {
            return false;
        }

        String[] parts = encodedHash.split("\\$", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Stored password hash has an invalid format.");
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            if (iterations < 100_000 || iterations > 5_000_000
                    || salt.length != SALT_LENGTH || expectedHash.length != HASH_LENGTH) {
                throw new IllegalArgumentException("Stored password hash has invalid parameters.");
            }

            byte[] actualHash = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Stored password hash has an invalid format.", e);
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, HASH_LENGTH * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("The password hashing algorithm is unavailable.", e);
        } finally {
            keySpec.clearPassword();
        }
    }
}
