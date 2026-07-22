package com.limtide.ugclite.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class AccountPreferenceNamespace {
    private AccountPreferenceNamespace() {
    }

    static String forUser(String prefix, String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(username.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder suffix = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                suffix.append(String.format("%02x", value & 0xff));
            }
            return prefix + "_" + suffix;
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
