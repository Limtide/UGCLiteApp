package com.limtide.ugclite.data.repository;

final class FeedPagination {

    private FeedPagination() {
    }

    static int nextOffset(int currentOffset, int receivedCount, boolean refresh) {
        int safeCount = Math.max(0, receivedCount);
        int baseOffset = refresh ? 0 : Math.max(0, currentOffset);
        return baseOffset + safeCount;
    }
}
