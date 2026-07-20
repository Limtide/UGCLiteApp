# P1 Bug Fix Log

## P1-01 Feed API response contract

- Root cause: the client only modeled `status_code/post_list`; README documents `code/data`, and missing primitive fields were interpreted as a successful status code of `0`.
- Fix: support both documented and legacy response envelopes, require a recognized status field, and select the matching post list.
- Regression coverage: `FeedResponseTest` covers documented success, legacy success, and an unknown object that must not be treated as success.
