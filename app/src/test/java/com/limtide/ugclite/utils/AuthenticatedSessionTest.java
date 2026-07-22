package com.limtide.ugclite.utils;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthenticatedSessionTest {
    @After
    public void clearSession() {
        AuthenticatedSession.clear();
    }

    @Test
    public void sessionStartsFailClosed() {
        AuthenticatedSession.clear();

        assertFalse(AuthenticatedSession.isAuthenticated());
    }

    @Test
    public void successfulAuthenticationEstablishesProcessSession() {
        AuthenticatedSession.establish(" alice ");

        assertTrue(AuthenticatedSession.isAuthenticated());
        assertEquals("alice", AuthenticatedSession.getAuthenticatedUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankIdentityCannotEstablishSession() {
        AuthenticatedSession.establish("   ");
    }

    @Test
    public void logoutClearsSession() {
        AuthenticatedSession.establish("alice");

        AuthenticatedSession.clear();

        assertFalse(AuthenticatedSession.isAuthenticated());
    }
}
