package com.limtide.ugclite.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeedLoadGateTest {

    @Test
    public void activeRequestRejectsAnotherRequest() {
        FeedLoadGate gate = new FeedLoadGate();
        assertTrue(gate.tryStart());
        assertFalse(gate.tryStart());
    }

    @Test
    public void finishedRequestAllowsNextRequest() {
        FeedLoadGate gate = new FeedLoadGate();
        assertTrue(gate.tryStart());
        gate.finish();
        assertTrue(gate.tryStart());
    }
}
