package com.limtide.ugclite.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyMigrationDecisionTest {

    @Test
    public void unclaimedLegacyDataIsCopiedOnce() {
        assertEquals(
                LegacyMigrationDecision.Action.COPY_AND_MARK,
                LegacyMigrationDecision.decide(false, false, true));
    }

    @Test
    public void existingTargetDataIsNeverOverwritten() {
        assertEquals(
                LegacyMigrationDecision.Action.MARK_ONLY,
                LegacyMigrationDecision.decide(false, true, true));
    }

    @Test
    public void completedMigrationCannotBeClaimedByAnotherAccount() {
        assertEquals(
                LegacyMigrationDecision.Action.SKIP,
                LegacyMigrationDecision.decide(true, false, true));
    }

    @Test
    public void missingLegacyDataStillCompletesMigration() {
        assertEquals(
                LegacyMigrationDecision.Action.MARK_ONLY,
                LegacyMigrationDecision.decide(false, false, false));
    }
}
