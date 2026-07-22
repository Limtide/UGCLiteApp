package com.limtide.ugclite.data.repository;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginAttemptLimiterTest {

    @Test
    public void fifthAccountFailureLocksPersistedBucket() {
        MemoryStore store = new MemoryStore();
        FakeClock clock = new FakeClock(1_000L);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(store, clock);

        for (int attempt = 0; attempt < LoginAttemptLimiter.ACCOUNT_FAILURE_LIMIT; attempt++) {
            limiter.recordFailure(" Alice ");
        }

        LoginAttemptLimiter recreated = new LoginAttemptLimiter(store, clock);
        assertFalse(recreated.isAllowed("alice"));

        clock.advance(LoginAttemptLimiter.LOCK_MILLIS);
        assertTrue(recreated.isAllowed("ALICE"));
    }

    @Test
    public void rotatingAccountsEventuallyTriggersDeviceLock() {
        MemoryStore store = new MemoryStore();
        FakeClock clock = new FakeClock(2_000L);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(store, clock);

        for (int attempt = 0; attempt < LoginAttemptLimiter.GLOBAL_FAILURE_LIMIT; attempt++) {
            limiter.recordFailure("user-" + attempt);
        }

        assertFalse(limiter.isAllowed("unused-account"));
    }

    @Test
    public void successfulLoginClearsOnlyAccountBucket() {
        MemoryStore store = new MemoryStore();
        FakeClock clock = new FakeClock(3_000L);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(store, clock);

        limiter.recordFailure("alice");
        limiter.recordFailure("bob");
        limiter.recordSuccess("alice");

        assertFalse(store.contains(LoginAttemptLimiter.accountBucketKey("alice")));
        assertTrue(store.contains(LoginAttemptLimiter.accountBucketKey("bob")));
        assertTrue(store.contains("global"));
    }

    @Test
    public void accountKeysAreNormalizedAndDoNotExposeUsername() {
        String first = LoginAttemptLimiter.accountBucketKey(" Alice ");
        String second = LoginAttemptLimiter.accountBucketKey("alice");

        assertTrue(first.equals(second));
        assertFalse(first.contains("alice"));
    }

    @Test
    public void clockRollbackFailsClosed() {
        MemoryStore store = new MemoryStore();
        FakeClock clock = new FakeClock(10_000L);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(store, clock);
        limiter.recordFailure("alice");

        clock.set(9_999L);

        assertFalse(limiter.isAllowed("alice"));
    }

    private static final class FakeClock implements LoginAttemptLimiter.Clock {
        private long now;

        FakeClock(long now) {
            this.now = now;
        }

        @Override
        public long nowMillis() {
            return now;
        }

        void advance(long duration) {
            now += duration;
        }

        void set(long now) {
            this.now = now;
        }
    }

    private static final class MemoryStore implements LoginAttemptLimiter.Store {
        private final Map<String, LoginAttemptLimiter.Bucket> buckets = new HashMap<>();

        @Override
        public LoginAttemptLimiter.Bucket read(String key) {
            LoginAttemptLimiter.Bucket bucket = buckets.get(key);
            return bucket == null ? new LoginAttemptLimiter.Bucket(0, 0, 0) : bucket;
        }

        @Override
        public void write(String key, LoginAttemptLimiter.Bucket bucket) {
            buckets.put(key, bucket);
        }

        @Override
        public void remove(String key) {
            buckets.remove(key);
        }

        boolean contains(String key) {
            return buckets.containsKey(key);
        }
    }
}
