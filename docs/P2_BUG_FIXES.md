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

## P2-04 Null or malformed posts turn a successful page into an error

- Root cause: Feed filtering and success logging dereferenced null lists, posts, and clips after the API callback reported success.
- Fix: normalize a null list to an empty page, skip null posts and clips, and compute null-safe counts before publishing one success result.
- Reliability behavior: malformed entries are discarded without failing the complete page or generating a second error event.
- Regression coverage: FeedPostFilterTest covers null lists, null entries, supported clips, and unsupported clips.

## P2-05 CacheManager callbacks and Glide memory cleanup never run

- Root cause: CacheManager retained an application Context but required it to be an Activity before dispatching callbacks or clearing Glide memory; that condition was always false.
- Fix: introduce a main-thread Handler and dispatch cleanup results, errors, statistics, and Glide.clearMemory through it.
- Lifecycle behavior: CacheManager continues retaining only the application Context and no Activity is leaked.
- Verification: Java compilation validates every callback path and Glide main-thread invocation.

## P2-06 Cache size enforcement over-deletes files

- Root cause: the cleanup loop read File.length after deletion, which normally returned zero, so its remaining-size counter never decreased.
- Fix: capture each file size before deletion, decrement the remaining size only after a successful delete, and include size-limit deletions in cleanup statistics.
- Retention behavior: cleanup stops as soon as the configured size threshold is reached instead of deleting every candidate.
- Verification: Java compilation validates both music and thumbnail cleanup paths; final review checks both loops use the pre-delete size.

## P2-07 Music download callback uses a released player

- Root cause: asynchronous cache callbacks retained the MusicPlayer after release and unconditionally accessed a null or released MediaPlayer.
- Fix: add a release flag and request generation; stale success, failure, and progress callbacks are ignored after release or after another URL replaces the request.
- Defensive behavior: source-loading methods capture and validate the current MediaPlayer and handle IllegalStateException as a normal playback error.
- Verification: Java compilation validates callback capture and lifecycle guards; final review checks release invalidates all outstanding generations.

## P2-08 Manual media swipe creates inconsistent mute state

- Root cause: the page-change callback modified the Activity flag and video players directly without updating the persistent MuteManager.
- Fix: manual swipe writes only through MuteManager.setMuted; its existing listener synchronizes the Activity flag, icon, video players, and music player.
- Resume behavior: the persisted manager state remains authoritative after pause and resume.
- Verification: Java compilation validates the single-state path; final review checks no direct swipe mutation remains.

## P2-09 Likes and follows leak across local accounts

- Root cause: LikeManager and FollowManager used one global SharedPreferences file and retained one process-wide cache across account switches.
- Fix: derive a non-identifying SHA-256 preference namespace from the authenticated username and reset both singleton caches whenever the authenticated identity changes or clears.
- Privacy behavior: each account retains its own interactions without exposing them to another account on the same device.
- Regression coverage: AccountPreferenceNamespaceTest covers stable namespaces, account separation, and removal of raw identity data from preference names.

## P2-10 Process session never expires

- Root cause: AuthenticatedSession stored only a username, so a long-lived process remained authorized indefinitely.
- Fix: record authentication time, enforce a 24-hour process-session TTL, and clear both memory and persisted display login state when the gate rejects access.
- Fail-closed behavior: expired sessions are redirected to LoginActivity from every protected Activity.
- Regression coverage: AuthenticatedSessionTest covers the TTL boundary and timestamp removal during logout.

## P2-11 Feed errors replace usable cached content

- Root cause: every load-more or refresh failure opened the full-screen empty-state overlay, even when the adapter still contained usable posts; the sticky error value could also display again after view recreation.
- Fix: keep existing posts visible and show a transient Toast for non-empty feeds; reserve the full-screen error state for an empty feed.
- Event behavior: clear the consumed error message immediately and hide the empty-state overlay whenever the ViewModel reports a non-empty state.
- Verification: compile the Fragment and exercise both empty and populated adapter branches during final validation.

## P2-12 Unlimited credential guessing and account enumeration

- Root cause: login accepted unlimited attempts and returned distinct messages for missing, disabled, and wrong-password accounts; internal exception details were also exposed to the UI.
- Fix: persist account-level (5 attempts) and device-level (20 attempts) buckets over a 15-minute window, lock both for 15 minutes, and serialize read-modify-write operations.
- Privacy behavior: normalize usernames and store only SHA-256 bucket keys; return one credential error for missing, disabled, and invalid-password accounts and keep exception details in logs.
- Timing behavior: missing, disabled, and failed legacy-password paths execute a dummy PBKDF2 verification to reduce account-existence timing differences.
- Regression coverage: LoginAttemptLimiterTest covers persisted lockout, device-wide protection against username rotation, account-only reset after success, opaque normalized keys, and fail-closed clock rollback.
- Residual risk: local rate limiting can be bypassed by clearing app data or reinstalling and does not replace server-side throttling; registration still reports username conflicts as part of its existing UX.

## P2-13 Queued refresh can publish a stale page

- Root cause: FeedRepository checked for a queued refresh separately from posting the completed page, so a refresh arriving between those operations allowed stale content to reach observers.
- Fix: FeedLoadGate now atomically chooses either to publish the completed result or consume the queued refresh and keep the gate active for the replacement request.
- Ordering behavior: a refresh linearized before completion suppresses the old success or error result; a refresh arriving after publication starts as the next request.
- Regression coverage: FeedLoadGateTest verifies stale-result suppression, active-state continuity, normal publication, and subsequent request admission.
