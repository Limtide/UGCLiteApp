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

## P1-07 First-launch completion

- Root cause: `first_launch` defaulted to true and its completion method was never called, so startup never reached later authentication decisions.
- Fix: when the app first presents its actual login UI, mark first launch complete without granting a login session.
- Regression coverage: `completingFirstLaunchDoesNotGrantLogin` verifies the flag transition and confirms that authentication is still required.
