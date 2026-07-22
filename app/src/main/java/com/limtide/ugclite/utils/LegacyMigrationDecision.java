package com.limtide.ugclite.utils;

final class LegacyMigrationDecision {
    enum Action {
        SKIP,
        MARK_ONLY,
        COPY_AND_MARK
    }

    private LegacyMigrationDecision() {
    }

    static Action decide(boolean claimed, boolean targetHasData, boolean legacyHasData) {
        if (claimed) {
            return Action.SKIP;
        }
        if (targetHasData || !legacyHasData) {
            return Action.MARK_ONLY;
        }
        return Action.COPY_AND_MARK;
    }
}
