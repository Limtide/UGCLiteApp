package com.limtide.ugclite.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    public void hashUsesRandomSaltAndVerifiesCorrectPassword() {
        String first = hasher.hash("demo123");
        String second = hasher.hash("demo123");

        assertNotEquals(first, second);
        assertTrue(hasher.verify("demo123", first));
        assertTrue(hasher.verify("demo123", second));
    }

    @Test
    public void wrongPasswordIsRejected() {
        String encoded = hasher.hash("demo123");

        assertFalse(hasher.verify("wrong-password", encoded));
    }

    @Test
    public void malformedAndLegacyHashesAreRejected() {
        assertFalse(hasher.verify("demo123", "not-a-password-hash"));
        assertFalse(hasher.verify("demo123", "e10adc3949ba59abbe56e057f20f883e"));
        assertTrue(hasher.isLegacyMd5("e10adc3949ba59abbe56e057f20f883e"));
    }

    @Test
    public void unicodePasswordRoundTrips() {
        String encoded = hasher.hash("密碼-安全-123");

        assertTrue(hasher.verify("密碼-安全-123", encoded));
    }

    @Test
    public void tamperedHashIsRejected() {
        String encoded = hasher.hash("demo123");
        String tampered = encoded.substring(0, encoded.length() - 2) + "00";

        assertFalse(hasher.verify("demo123", tampered));
    }
}
