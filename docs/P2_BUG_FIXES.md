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
