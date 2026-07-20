package com.limtide.ugclite.data.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeedPaginationTest {

    @Test
    public void refreshAdvancesPastFirstPage() {
        assertEquals(20, FeedPagination.nextOffset(0, 20, true));
    }

    @Test
    public void loadMoreAdvancesFromCurrentOffset() {
        assertEquals(40, FeedPagination.nextOffset(20, 20, false));
    }

    @Test
    public void offsetUsesRawResponseCountNotFilteredCount() {
        int rawResponseCount = 20;
        int filteredDisplayCount = 7;

        int nextOffset = FeedPagination.nextOffset(0, rawResponseCount, true);

        assertEquals(20, nextOffset);
        assertEquals(13, nextOffset - filteredDisplayCount);
    }
}
