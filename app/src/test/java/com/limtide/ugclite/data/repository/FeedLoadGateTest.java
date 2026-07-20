package com.limtide.ugclite.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeedLoadGateTest {

    @Test
    public void activeLoadMoreRejectsAnotherLoadMore() {
        FeedLoadGate gate = new FeedLoadGate();
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertEquals(FeedLoadGate.Decision.REJECTED, gate.request(false));
        assertFalse(gate.hasQueuedRefresh());
    }

    @Test
    public void refreshDuringActiveRequestIsQueued() {
        FeedLoadGate gate = new FeedLoadGate();
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertEquals(FeedLoadGate.Decision.QUEUED, gate.request(true));
        assertTrue(gate.hasQueuedRefresh());
        assertTrue(gate.completeAndShouldStartRefresh());
        assertTrue(gate.isActive());
        assertFalse(gate.completeAndShouldStartRefresh());
        assertFalse(gate.isActive());
    }

    @Test
    public void completedRequestAllowsNextRequest() {
        FeedLoadGate gate = new FeedLoadGate();
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
        assertFalse(gate.completeAndShouldStartRefresh());
        assertEquals(FeedLoadGate.Decision.STARTED, gate.request(false));
    }
}
