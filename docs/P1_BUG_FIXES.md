# P1 Bug Fix Log

## P1-01 Feed API response contract

- Root cause: the client only modeled `status_code/post_list`; README documents `code/data`, and missing primitive fields were interpreted as a successful status code of `0`.
- Fix: support both documented and legacy response envelopes, require a recognized status field, and select the matching post list.
- Regression coverage: `FeedResponseTest` covers documented success, legacy success, and an unknown object that must not be treated as success.

## P1-02 Feed pagination offset

- Root cause: refresh left the cursor at `0`, so the first load-more repeated page one; later offsets used the filtered display count.
- Fix: advance the offset after every successful page, using the raw response count rather than the filtered count.
- Regression coverage: `FeedPaginationTest` covers refresh, load-more, and filtering-independent offset calculation.
- Contract note: the current API exposes no `next_cursor`; this is the deterministic offset fallback until the backend provides one.

## P1-03 Refresh request collision

- Root cause: the ViewModel enabled the refresh spinner before the repository silently rejected an overlapping request.
- Fix: use an atomic request gate, return whether a request started, and clear UI loading immediately when refresh is rejected.
- Regression coverage: `FeedLoadGateTest` proves that overlapping requests are rejected atomically and the gate reopens after completion.
- Independent-review follow-up: refreshes that arrive during another request are queued, stale results are suppressed, and the queued refresh starts when the active request completes.

## P1-04 Feed repository executor lifetime

- Root cause: clearing one page-level ViewModel shut down the executor owned by the process-wide repository singleton.
- Fix: remove the ViewModel-triggered shutdown path; the singleton executor now remains valid for later Feed screens in the same process.
- Verification: Java compilation covers the removed API call; lifecycle behavior is included in the final device-test checklist.

## P1-05 Feed observer lifetime

- Root cause: an anonymous `observeForever` observer kept cleared ViewModels reachable and continued delivering singleton results.
- Fix: retain the Observer instance and remove it in `onCleared()`.
- Verification: Java compilation verifies the observer type and removal path; repeated screen recreation remains in the device-test checklist.

## P1-06 Fragment recreation

- Root cause: every Activity creation added new Home and Profile fragments even though FragmentManager had already restored the old instances.
- Fix: reuse fragments by tag, add only missing instances, and persist/restore the selected tab.
- Regression coverage: `MainActivityRecreationTest` recreates the Activity and asserts exactly two root fragments with the Profile tab still visible.
- Independent-review follow-up: restored `currentFragment` now follows the actually visible fragment, including Profile followed by a placeholder tab before recreation.

## P1-07 First-launch completion

- Root cause: `first_launch` defaulted to true and its completion method was never called, so startup never reached later authentication decisions.
- Fix: when the app first presents its actual login UI, mark first launch complete without granting a login session.
- Regression coverage: `completingFirstLaunchDoesNotGrantLogin` verifies the flag transition and confirms that authentication is still required.

## P1-08 Fail-closed session handling

- Root cause: the app generated a predictable token locally and treated local preference flags as successful server authentication.
- Fix: remove simulated auto-login and token generation; cold start now requires interactive login until a real authentication backend can validate a session.
- Regression coverage: `forgedLocalSessionCannotAutoLogin` proves that even a fully populated forged local state cannot bypass login.
- Product impact: the remember option preserves the user's preference only; it no longer grants automatic authentication.

## P1-09 Sensitive logging

- Root cause: startup dumped every preference value, API handling logged full response bodies, and login paths logged identity values.
- Fix: remove preference dumps and response-body logging, and replace identity-bearing messages with fixed event text.
- Verification: a source scan confirms that `printAllPreferences` and full response logging no longer exist.

## P1-10 Password hashing

- Root cause: passwords were stored as unsalted MD5 values and compared directly in a Room SQL query.
- Fix: use salted PBKDF2 records with bounded iteration parsing and constant-time hash comparison; query users by username before verification.
- Legacy handling: an existing MD5 record is verified only during login migration; a correct password is immediately rehashed as PBKDF2, while an incorrect password remains rejected.
- Regression coverage: `PasswordHasherTest` covers random salts, correct and incorrect passwords, Unicode, malformed/legacy values, and tampering.
- Removed artifact: `MD5Utils.java` was deleted and remains recoverable from Git history.
- Independent-review follow-up: production startup no longer seeds a public `demo/demo123` account; test fixtures must be created only inside test source sets.
- Upgrade cleanup: Room migration 1 to 2 removes the previously seeded `demo` row so upgraded installations cannot retain the public credential.

## P1-11 Fresh-install account creation

- Root cause: production demo-account seeding was removed, but the registration control still displayed a placeholder, leaving a fresh installation with no valid account source.
- Fix: validate the entered username and password, create a local active user, hash the password with PBKDF2 unconditionally, and continue through the existing successful-authentication flow.
- Security constraint: callers cannot bypass hashing by supplying a string that merely looks like an encoded password.
- Verification: Java compilation covers the ViewModel-to-repository registration path; the final Android test build verifies the UI-linked code, while device execution is listed separately when unavailable.

## P1-12 Asynchronous authentication identity confusion

- Root cause: the login result authenticated one immutable User, but persistence reread a still-editable username field after the asynchronous database operation.
- Fix: persist only the username carried by the successful LoginResult and fail closed if a success result has no User.
- Concurrency guard: disable username, password, and registration controls while authentication or registration is in progress.
- Verification: compilation covers the result-to-persistence contract; independent review checks that mutable input is no longer used after authentication.

## P1-13 Restored MainActivity bypasses authentication

- Root cause: MainActivity trusted Android task restoration and never checked whether the current process had completed a real login or registration.
- Fix: add an in-memory authenticated session that is established only from a successful LoginResult and checked in MainActivity onCreate and onResume.
- Fail-closed behavior: process death clears the session, so a restored MainActivity clears the task and redirects to LoginActivity.
- Logout behavior: the active profile logout path clears both the process session and persisted display state.
- Regression coverage: AuthenticatedSessionTest covers fail-closed startup, successful establishment, invalid identities, and logout.

## P1-14 Protected Activity authentication coverage

- Root cause: only MainActivity checked the process session, while Android can restore PostDetailActivity or HashtagActivity directly as the task-stack top.
- Fix: centralize the fail-closed redirect in AuthenticationGate and require it from every Manifest-registered non-login Activity during onCreate and onResume.
- Restored-task behavior: a missing process session clears the task and redirects to LoginActivity before protected UI initialization.
- Regression coverage: ProtectedActivityAuthenticationTest launches each protected Activity without a session and verifies it cannot remain resumed.
- Scope check: the Manifest currently registers MainActivity, PostDetailActivity, and HashtagActivity as protected Activities; LoginActivity remains the only public entry.
