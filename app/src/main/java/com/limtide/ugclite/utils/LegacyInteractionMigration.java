package com.limtide.ugclite.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

final class LegacyInteractionMigration {
    private static final String MARKERS_NAME = "account_interaction_migrations";
    private static final Object LOCK = new Object();

    private LegacyInteractionMigration() {
    }

    static void claimStringSet(
            Context context,
            String legacyPreferencesName,
            SharedPreferences target,
            String dataKey,
            String markerKey) {
        synchronized (LOCK) {
            SharedPreferences markers = context.getSharedPreferences(
                    MARKERS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences legacy = context.getSharedPreferences(
                    legacyPreferencesName,
                    Context.MODE_PRIVATE);
            Set<String> legacyValues = legacy.getStringSet(dataKey, null);
            LegacyMigrationDecision.Action action = LegacyMigrationDecision.decide(
                    markers.getBoolean(markerKey, false),
                    target.contains(dataKey),
                    legacyValues != null && !legacyValues.isEmpty());
            if (action == LegacyMigrationDecision.Action.SKIP) {
                return;
            }
            if (action == LegacyMigrationDecision.Action.COPY_AND_MARK) {
                boolean copied = target.edit()
                        .putStringSet(dataKey, new HashSet<>(legacyValues))
                        .commit();
                if (!copied) {
                    throw new IllegalStateException("Unable to migrate legacy interaction data");
                }
            }
            persistMarker(markers, markerKey);
        }
    }

    private static void persistMarker(SharedPreferences markers, String markerKey) {
        if (!markers.edit().putBoolean(markerKey, true).commit()) {
            throw new IllegalStateException("Unable to persist interaction migration marker");
        }
    }
}
