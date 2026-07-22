package com.limtide.ugclite.utils;

public final class AuthenticatedSession {
    private static volatile String authenticatedUsername;

    private AuthenticatedSession() {
    }

    public static void establish(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Authenticated username is required");
        }
        authenticatedUsername = username.trim();
    }

    public static boolean isAuthenticated() {
        return authenticatedUsername != null;
    }

    public static String getAuthenticatedUsername() {
        return authenticatedUsername;
    }

    public static void clear() {
        authenticatedUsername = null;
    }
}
