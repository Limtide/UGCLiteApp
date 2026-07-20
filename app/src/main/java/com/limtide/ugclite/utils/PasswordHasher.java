package com.limtide.ugclite.utils;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {

    private static final String FORMAT_ALGORITHM = "pbkdf2_sha1";
    private static final String JCA_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int MAX_ITERATIONS = 1_000_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom secureRandom;

    public PasswordHasher() {
        this(new SecureRandom());
    }

    PasswordHasher(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derivedKey = derive(password.toCharArray(), salt, ITERATIONS);
        return FORMAT_ALGORITHM + "$" + ITERATIONS + "$" + toHex(salt) + "$" + toHex(derivedKey);
    }

    public boolean verify(String password, String storedValue) {
        if (password == null || password.isEmpty() || storedValue == null) {
            return false;
        }

        String[] parts = storedValue.split("\\$", -1);
        if (parts.length != 4 || !FORMAT_ALGORITHM.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 10_000 || iterations > MAX_ITERATIONS) {
                return false;
            }
            byte[] salt = fromHex(parts[2]);
            byte[] expected = fromHex(parts[3]);
            if (salt.length != SALT_LENGTH_BYTES || expected.length == 0) {
                return false;
            }
            byte[] actual = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isEncoded(String storedValue) {
        return storedValue != null && storedValue.startsWith(FORMAT_ALGORITHM + "$");
    }

    public boolean isLegacyMd5(String storedValue) {
        return storedValue != null && storedValue.matches("(?i)^[0-9a-f]{32}$");
    }

    public boolean verifyLegacyMd5(String password, String storedValue) {
        if (password == null || password.isEmpty() || !isLegacyMd5(storedValue)) {
            return false;
        }

        try {
            MessageDigest legacyDigest = MessageDigest.getInstance("MD5");
            byte[] actual = legacyDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] expected = fromHex(storedValue);
            return MessageDigest.isEqual(expected, actual);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Legacy password migration is unavailable", exception);
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(JCA_ALGORITHM).generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            spec.clearPassword();
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private static byte[] fromHex(String value) {
        if (value == null || value.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexadecimal value");
        }
        byte[] result = new byte[value.length() / 2];
        for (int index = 0; index < value.length(); index += 2) {
            int high = Character.digit(value.charAt(index), 16);
            int low = Character.digit(value.charAt(index + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid hexadecimal value");
            }
            result[index / 2] = (byte) ((high << 4) | low);
        }
        return result;
    }
}
