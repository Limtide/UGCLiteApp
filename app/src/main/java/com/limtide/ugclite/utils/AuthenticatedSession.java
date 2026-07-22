package com.limtide.ugclite.utils;

public final class AuthenticatedSession {
    private static volatile String authenticatedUsername;
    static final long SESSION_TTL_MILLIS = 24L * 60 * 60 * 1000;
    private static volatile long authenticatedAtMillis;

    private AuthenticatedSession() {
    }

    public static void establish(String username) {
        establishAt(username, System.currentTimeMillis());
    }

    static void establishAt(String username, long authenticatedAt) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Authenticated username is required");
        }
        String normalizedUsername = username.trim();
        if (!normalizedUsername.equals(authenticatedUsername)) {
            LikeManager.resetInstance();
            FollowManager.resetInstance();
        }
        authenticatedUsername = normalizedUsername;
        authenticatedAtMillis = authenticatedAt;
    }

    public static boolean isAuthenticated() {
        return isAuthenticatedAt(System.currentTimeMillis());
    }

    static boolean isAuthenticatedAt(long nowMillis) {
        long sessionAge = nowMillis - authenticatedAtMillis;
        boolean valid = authenticatedUsername != null
                && authenticatedAtMillis > 0
                && sessionAge >= 0
                && sessionAge <= SESSION_TTL_MILLIS;
        return valid;
    }

    public static String getAuthenticatedUsername() {
        return authenticatedUsername;
    }

    public static void clear() {
        authenticatedUsername = null;
        LikeManager.resetInstance();
        FollowManager.resetInstance();
        authenticatedAtMillis = 0;
    }
}
