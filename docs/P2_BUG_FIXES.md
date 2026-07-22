# P2 Bug Fix Log

## P2-01 Sensitive local data included in Android backup

- Root cause: the application enabled Android backup while storing local account records, password hashes, login state, likes, and follows.
- Fix: disable application backup so Room and authentication-related SharedPreferences are not copied to cloud backup or device-transfer archives.
- Security behavior: restoring the application requires a fresh local account and authentication instead of restoring credential material.
- Verification: manifest processing and the final Android build validate the resulting application configuration.

## P2-02 Missing Room migration silently deletes accounts

- Root cause: fallbackToDestructiveMigration allowed Room to erase the complete user database whenever an upgrade path was missing.
- Fix: remove the destructive production fallback and require an explicit migration for every supported schema upgrade.
- Failure behavior: an unsupported schema now fails visibly instead of irreversibly deleting local accounts and profiles.
- Verification: Java compilation validates the Room builder configuration; migration 1 to 2 remains explicitly registered.

## P2-03 Empty refresh retains stale Feed content

- Root cause: FeedViewModel only replaced posts and updated hasMore when a successful response contained visible posts.
- Fix: every successful refresh replaces the list, including an empty or null list, and every successful response updates hasMore.
- UI behavior: HomeFragment now sends every non-null list to the adapter, allowing an empty refresh to remove previously clickable rows.
- Regression coverage: FeedListMergerTest covers empty refresh, null normalization, append behavior, and non-mutation of the previous list.
