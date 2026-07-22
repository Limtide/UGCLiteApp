# P2 Bug Fix Log

## P2-01 Sensitive local data included in Android backup

- Root cause: the application enabled Android backup while storing local account records, password hashes, login state, likes, and follows.
- Fix: disable application backup so Room and authentication-related SharedPreferences are not copied to cloud backup or device-transfer archives.
- Security behavior: restoring the application requires a fresh local account and authentication instead of restoring credential material.
- Verification: manifest processing and the final Android build validate the resulting application configuration.
