# Auction-house change review — 2026-09-08

Reviewed the current working-tree changes against the earlier auction fixes and audit. Changes are mixed in the working tree rather than isolated in a single Claude commit.

## Findings corrected in this review

- **Critical — automatic refunds after a possible commit.** BIN/bid exception handlers refunded even when the service had accepted the write or only a later notification failed. Reservations now track submission; automatic exception refunds occur only before submission. Explicit service rejections still refund. This initial guard has now been superseded by durable recovery for #36; see the recovery report below.
- **High — old listings fail after the long-price migration.** Mongo `getLong` cannot read BSON integers. Readers now accept either numeric representation. Old embedded creation receipts are normalized for comparison too, preventing a valid retry from being rejected and returning an already-listed item.
- **High — malformed bid history breaks an entire browser result.** Validate records before sorting, so one unreadable record is skipped instead of crashing all results.
- **Medium — cache hits can show newly expired listings.** Apply expiry filtering when returning cached results as well as when loading them.

## Changes retained

Long-price serialization, current-bid price sorting, descending bid-count sorting, non-destructive expiry filtering, bounded custom durations, near-expiry time extension, and client-side eligibility checks remain in place. The timing implementation is code-reviewed; exact live Hypixel parity is not asserted here.

## Remaining concerns (updated after recovery implementation)

- **#36:** durable bid/BIN recovery is now implemented and covered by fault/concurrency tests. See [the recovery report](auction-house-recovery.md); live verification is pending.
- **#42:** seller ownership, service-side profile/co-op checks, legacy resolution, and automatic creation recovery are now implemented. See [the ownership report](auction-house-profile-ownership.md) for the 85-test validation; in-game verification is pending.
- **#39:** the idempotent archival lifecycle is implemented and preserves claim receipts; see [the archive report](auction-house-archive.md).
- **#41:** pagination is implemented for browser, owned listings, and bids; see [the pagination report](auction-house-pagination.md) for the 92-test validation.
- **#46:** browser freshness/correlation and durable auction statistics are implemented; see [the statistics report](auction-house-statistics.md).
- **#43/#44/#45:** service-side eligibility and full rule/fee parity remain separate work.

## Follow-up inspection — 2026-09-08 (before the recovery fix)

Rechecked the bid/BIN callbacks, reservation state, claim eligibility, and browser refresh paths. The following remain reproducible from the control flow:

- **High (#36):** after `markSubmitted()`, an exceptional response leaves the reservation deducted. There is no persisted operation ID or reconciliation in `AuctionCoinReservation`, so a request that never commits can strand coins; a committed request with a lost response also misses active-bid tracking.
- **High (#42):** successful bid/BIN callbacks skip active-bid tracking when the player disconnects or changes profiles. `AuctionClaims` requires that tracking entry before requesting or delivering a receipt, so the original profile can be unable to collect its item/refund through the normal flow.
- **Medium (#41):** the browser selects page 1 of 24 results without navigation; Manage Auctions and View Bids display only their first seven entries. Additional entries cannot be reached in those menus.
- **Medium (#46):** periodic browser refresh checks service availability and redraws existing data instead of fetching listings. Overlapping filter/category requests also have no response ordering guard, allowing an older response to replace newer results.

Reran the isolated audit probe against the built service jar: all six checks passed (item preservation, write acknowledgement, immutable sorting, expiry filtering, missing-ID response, and long prices). This probe does not cover the client failure paths above. No running Docker containers were present; this follow-up made no production changes and did not perform in-game validation.

## Initial review validation

55 targeted tests passed with zero failures or skips. Coverage includes isolated Mongo concurrency and claim receipts, old/new numeric prices and creation retries, malformed listings, cached expiry, reservation refund boundaries, claim persistence, and Admin GUI catalog/permission/input checks. The isolated auction audit probe passed. Both jars passed full ZIP integrity checks.

Updated jars are installed and hash-verified in the stopped containers and persistent overrides. The server stack was already stopped and was left stopped; new in-game validation is pending. No live player balances, inventories, or listings were edited.
