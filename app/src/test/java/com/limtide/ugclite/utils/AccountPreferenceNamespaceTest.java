package com.limtide.ugclite.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class AccountPreferenceNamespaceTest {
    @Test
    public void sameUserGetsStableNamespace() {
        assertEquals(
                AccountPreferenceNamespace.forUser("likes", "alice"),
                AccountPreferenceNamespace.forUser("likes", " alice "));
    }

    @Test
    public void differentUsersGetDifferentNamespaces() {
        assertNotEquals(
                AccountPreferenceNamespace.forUser("likes", "alice"),
                AccountPreferenceNamespace.forUser("likes", "bob"));
    }

    @Test
    public void namespaceDoesNotExposeUsername() {
        String namespace = AccountPreferenceNamespace.forUser("likes", "alice@example.com");
        assertFalse(namespace.contains("alice"));
        assertFalse(namespace.contains("@"));
    }
}
