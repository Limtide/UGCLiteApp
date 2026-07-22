package com.limtide.ugclite.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class LoginAttemptLimiter {
    static final int ACCOUNT_FAILURE_LIMIT = 5;
    static final int GLOBAL_FAILURE_LIMIT = 20;
    static final long WINDOW_MILLIS = 15L * 60 * 1000;
    static final long LOCK_MILLIS = 15L * 60 * 1000;

    private static final String GLOBAL_BUCKET = "global";
    private static final Object LOCK = new Object();

    interface Clock {
        long nowMillis();
    }

    interface Store {
        Bucket read(String key);
        void write(String key, Bucket bucket);
        void remove(String key);
    }

    static final class Bucket {
        final int failures;
        final long windowStartedAt;
        final long lockedUntil;

        Bucket(int failures, long windowStartedAt, long lockedUntil) {
            this.failures = failures;
            this.windowStartedAt = windowStartedAt;
            this.lockedUntil = lockedUntil;
        }
    }

    private final Store store;
    private final Clock clock;

    public LoginAttemptLimiter(Context context) {
        this(new SharedPreferencesStore(context.getApplicationContext()), System::currentTimeMillis);
    }

    LoginAttemptLimiter(Store store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public boolean isAllowed(String username) {
        synchronized (LOCK) {
            long now = clock.nowMillis();
            return !isLocked(store.read(accountBucketKey(username)), now)
                    && !isLocked(store.read(GLOBAL_BUCKET), now);
        }
    }

    public void recordFailure(String username) {
        synchronized (LOCK) {
            long now = clock.nowMillis();
            increment(accountBucketKey(username), ACCOUNT_FAILURE_LIMIT, now);
            increment(GLOBAL_BUCKET, GLOBAL_FAILURE_LIMIT, now);
        }
    }

    public void recordSuccess(String username) {
        synchronized (LOCK) {
            store.remove(accountBucketKey(username));
        }
    }

    static String accountBucketKey(String username) {
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder("account_");
            for (byte value : digest) {
                result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void increment(String key, int limit, long now) {
        Bucket current = store.read(key);
        if (current.failures > 0 && now < current.windowStartedAt) {
            store.write(key, new Bucket(
                    current.failures,
                    current.windowStartedAt,
                    Math.max(current.lockedUntil, current.windowStartedAt + LOCK_MILLIS)));
            return;
        }

        int failures;
        long windowStartedAt;
        if (current.failures == 0 || now - current.windowStartedAt >= WINDOW_MILLIS) {
            failures = 1;
            windowStartedAt = now;
        } else {
            failures = current.failures + 1;
            windowStartedAt = current.windowStartedAt;
        }
        long lockedUntil = failures >= limit ? now + LOCK_MILLIS : current.lockedUntil;
        store.write(key, new Bucket(failures, windowStartedAt, lockedUntil));
    }

    private static boolean isLocked(Bucket bucket, long now) {
        return bucket.lockedUntil > now
                || (bucket.failures > 0 && now < bucket.windowStartedAt);
    }

    private static final class SharedPreferencesStore implements Store {
        private static final String PREFERENCES_NAME = "login_attempt_limits";
        private final SharedPreferences preferences;

        SharedPreferencesStore(Context context) {
            preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

        @Override
        public Bucket read(String key) {
            return new Bucket(
                    preferences.getInt(key + ".failures", 0),
                    preferences.getLong(key + ".window", 0),
                    preferences.getLong(key + ".locked", 0));
        }

        @Override
        public void write(String key, Bucket bucket) {
            boolean persisted = preferences.edit()
                    .putInt(key + ".failures", bucket.failures)
                    .putLong(key + ".window", bucket.windowStartedAt)
                    .putLong(key + ".locked", bucket.lockedUntil)
                    .commit();
            if (!persisted) {
                throw new IllegalStateException("Unable to persist login attempt state");
            }
        }

        @Override
        public void remove(String key) {
            boolean persisted = preferences.edit()
                    .remove(key + ".failures")
                    .remove(key + ".window")
                    .remove(key + ".locked")
                    .commit();
            if (!persisted) {
                throw new IllegalStateException("Unable to clear login attempt state");
            }
        }
    }
}
