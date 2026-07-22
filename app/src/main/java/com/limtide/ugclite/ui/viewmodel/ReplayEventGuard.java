package com.limtide.ugclite.ui.viewmodel;

final class ReplayEventGuard<T> {
    private final T replayedValue;

    ReplayEventGuard(T replayedValue) {
        this.replayedValue = replayedValue;
    }

    boolean shouldDeliver(T value) {
        return value != replayedValue;
    }
}
