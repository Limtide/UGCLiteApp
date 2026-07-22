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

    synchronized boolean complete(Runnable publisher) {
        if (refreshQueued) {
            refreshQueued = false;
            return true;
        }
        publisher.run();
        active = false;
        return false;
    }

    synchronized boolean isActive() {
        return active;
    }
}
