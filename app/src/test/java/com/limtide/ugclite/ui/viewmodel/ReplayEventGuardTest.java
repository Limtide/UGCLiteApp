package com.limtide.ugclite.ui.viewmodel;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReplayEventGuardTest {
    @Test
    public void replayedValuePresentBeforeSubscriptionIsIgnored() {
        Object stalePage = new Object();
        ReplayEventGuard<Object> guard = new ReplayEventGuard<>(stalePage);

        assertFalse(guard.shouldDeliver(stalePage));
    }

    @Test
    public void newlyPublishedValueIsDelivered() {
        Object stalePage = new Object();
        Object refreshedPage = new Object();
        ReplayEventGuard<Object> guard = new ReplayEventGuard<>(stalePage);

        assertTrue(guard.shouldDeliver(refreshedPage));
    }

    @Test
    public void distinctEqualLookingValueIsStillANewEvent() {
        String stalePage = new String("page");
        String refreshedPage = new String("page");

        assertTrue(new ReplayEventGuard<>(stalePage).shouldDeliver(refreshedPage));
    }
}
