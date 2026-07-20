package com.limtide.ugclite.data.repository;

final class FeedLoadGate {

    enum Decision {
        STARTED,
        QUEUED,
        REJECTED
    }

    private boolean active;
    private boolean refreshQueued;

    synchronized Decision request(boolean refresh) {
        if (!active) {
            active = true;
            return Decision.STARTED;
        }
        if (refresh) {
            refreshQueued = true;
            return Decision.QUEUED;
        }
        return Decision.REJECTED;
    }

    synchronized boolean hasQueuedRefresh() {
        return refreshQueued;
    }

    synchronized boolean completeAndShouldStartRefresh() {
        if (refreshQueued) {
            refreshQueued = false;
            return true;
        }
        active = false;
        return false;
    }

    synchronized boolean isActive() {
        return active;
    }
}
