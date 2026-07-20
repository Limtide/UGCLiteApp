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
