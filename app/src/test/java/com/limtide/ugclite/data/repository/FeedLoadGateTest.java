package com.limtide.ugclite.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class FeedLoadGateTest {

    @Test
    public void activeLoadMoreRejectsAnotherLoadMore() {
        FeedLoadGate gate = new FeedLoadGate();
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertEquals(FeedLoadGate.Decision.REJECTED, gate.request(false));
    }

    @Test
    public void queuedRefreshSuppressesCompletedResult() {
        FeedLoadGate gate = new FeedLoadGate();
        AtomicBoolean published = new AtomicBoolean();

        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertEquals(FeedLoadGate.Decision.QUEUED, gate.request(true));
        assertTrue(gate.complete(() -> published.set(true)));

        assertFalse(published.get());
        assertTrue(gate.isActive());
        assertFalse(gate.complete(() -> published.set(true)));
        assertTrue(published.get());
        assertFalse(gate.isActive());
    }

    @Test
    public void completedRequestPublishesAndAllowsNextRequest() {
        FeedLoadGate gate = new FeedLoadGate();
        AtomicBoolean published = new AtomicBoolean();

        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertFalse(gate.complete(() -> published.set(true)));

        assertTrue(published.get());
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
    }
}
