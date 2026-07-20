package com.limtide.ugclite.data.repository;

import java.util.concurrent.atomic.AtomicBoolean;

final class FeedLoadGate {

    private final AtomicBoolean active = new AtomicBoolean(false);

    boolean tryStart() {
        return active.compareAndSet(false, true);
    }

    void finish() {
        active.set(false);
    }

    boolean isActive() {
        return active.get();
    }
}
