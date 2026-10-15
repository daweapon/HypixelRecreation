# Auction house audit

Audited 2026-09-07 (local time). **Audit complete; repairs underway.** Transaction and browsing defects prevent calling the auction house reliable.

## Scope and verification

Reviewed NPC entry, creation/escrow, price/duration input, browsing/filtering/sorting, listing details, BIN buying, normal bids, expiry, seller proceeds, buyer items, losing-bid refunds, owned/bid lists, profile/co-op checks and stats.

The running service initialized normally. Direct read-only Mongo inspection found **4 active records, including 3 already expired; 2 inactive records; no overlapping IDs**. No player balances, inventories or listings were changed. Live browse endpoints were not invoked because they can archive records.

An isolated process using a copy of the deployed service jar and fake Mongo collections reproduced six failures. No real database or Redis connection is used by that probe. Race/crash findings are inferred from source, not claims of observed live exploitation. No two-player in-game transaction test was performed.

## Repair progress

- Bugs & Loose Ends #36: **Implemented** — bid/BIN reservations and outcomes are durable and replayable; original-profile tracking recovers on reload. See [auction-house-recovery.md](auction-house-recovery.md) for the 71-test validation and deployment details. The remaining #42 seller/co-op identity work is now implemented; see [the ownership report](auction-house-profile-ownership.md).

- Bugs & Loose Ends #30: **Done** — removed the item-type overwrite; insert/update metadata and missing-legacy-attribute regression tests passed. New service jar validated and deployed; startup and deployed hash verified. Existing listings were not rewritten.
- Bugs & Loose Ends #31: **Done** — BIN rejection paths now refund their original reservation once. Four tests cover exact refund, repeated refund, intervening credits, invalid amounts and concurrent reservations. Validated jar and deployed matching builds to all three game servers; startup verified. Durable settlement and profile lifecycle coordination remain separate work.
- Bugs & Loose Ends #32: **Done** — insert-only creates and conditional Mongo updates prevent competing requests from replacing winners. Service validates bid state/price/expiry/owner and preserves item/category. Clients refund explicit conflicts and track bids only after success. Twelve tests passed, including isolated MongoDB races with 24 contenders, protocol round-trip, stale-state rejection, metadata preservation and refund regressions.
- Bugs & Loose Ends #33: **Done** — creation saves pending item/fee escrow before sending, retries the same listing ID without charging twice, records ownership only after confirmed persistence, and restores item/fee once on explicit rejection. Unknown outcomes remain pending with a Check Pending Auction action. Escrow survives serialization/reload; callbacks check the original profile under its lifecycle lock. Twenty-one targeted tests passed (zero skipped), including concurrent creation retries, retries after sale/expiry, reload recovery and exact refunds. Both jars passed full ZIP validation and matching hashes/startup were verified on the service and all three game servers. No in-game failure-injection test was performed.
- Bugs & Loose Ends #34: **Done** — losing claims return the sum of all accepted bids; winners reclaim all earlier bids and pay only the winning price. Existing unclaimed histories use the same full-charge accounting. Claim handlers recheck eligibility and remove every duplicate tracking entry; future bids avoid duplicate entries. Fifteen targeted tests passed, including five refund/accounting regressions. Full ZIP integrity, matching persistent/deployed hashes and successful startup verified on all three game servers. Already-claimed historical losses are not automatically compensated; durable crash-safe settlement remains #35.
- Bugs & Loose Ends #35: **Implemented and installed; in-game claim confirmation pending** — Mongo conditionally records one immutable claim receipt per auction/player, bound to the original profile. All seven seller/buyer/expired/refund paths use the same claim flow. Profile delivery checks the persisted receipt inside a storage transaction; inventory, coins, claim tracking and the profile snapshot commit together. Claim coin changes bypass individual queued writes. Older snapshots cannot erase delivered receipts, duplicate tracking is cleared, and existing completed legacy claims stay completed. Full inventory leaves the receipt retryable; save failures disconnect before further interaction. Forty-six tests passed with zero skips, including real isolated Mongo races (24 contenders), receipt retries/profile binding, all entitlement types, simulated pre/post-commit failures, atomic profile writes and stale-snapshot protection. No live player balances/listings were edited and no in-game crash injection was performed.
- #39 is complete: browsing filters expired results, while an independent idempotent lifecycle archives ended listings without losing concurrent claim/profile updates. See [auction-house-archive.md](auction-house-archive.md).
- Menu repair: #38 and #40 **Done**; deployed/override hashes and startup verified on service and all three game servers. Filter sorting copies cached lists; missing listings return explicit serializable failure with active-record precedence. All fetch consumers handle missing results; owned/bid lists wait for result processing. Browser controls render on open and response receipt; half-list erasure corrected. #41 pagination is now implemented for all three menus; see [the pagination report](auction-house-pagination.md). Eighteen targeted tests and the isolated audit probe passed.
- #46 is now complete: sorting and browser freshness fixes are joined by durable seller/buyer statistics updates. See [auction-house-statistics.md](auction-house-statistics.md).
- Other findings remain open.

## Critical transaction findings

1. **P0 — Item metadata overwritten (fixed).** Before the fix, `EndpointAddItem.handle` unconditionally set `item_type` to `HYPERION` on every create/update. The deployed-service probe reproduces a STONE attribute becoming HYPERION. Missing attributes also cause a null dereference.
2. **P0 — Failed BIN purchase can mint coins (fixed).** Previously, `AuctionViewThirdBin` captured starting balance C, deducts price P, then sets C + P on already-sold/same-co-op rejection. A 1,000-coin balance and rejected 100-coin purchase becomes 1,100. The balance snapshot can also overwrite unrelated balance changes.
3. **P0 — Purchases/bids are not atomic (fixed).** Previously, clients fetched a copy and submitted arbitrary replacements through `EndpointAddItem`; no compare-and-set, version check, operation ID or service-side bid/buy validation. Two purchasers can both pay and receive success while the last write chooses the winner. Code-derived race, not live-reproduced.
4. **P0 — Success before persistence; escrow lost on failures (fixed).** Originally the service acknowledged before its database write completed and the client cleared escrow/added ownership without checking success. #32 made writes synchronous; #33 adds persisted pending creation, stable retry identity, fee/item recovery on rejection and duplicate-submit protection. Late retries retain the original end time, so an already-ended listing remains ended and cannot be purchased. BIN/bid transport failures and claim settlement remain separate findings.
5. **P0 — Repeat bids strand coins (fixed).** Previously every accepted bid charged its full amount but losing claims returned only the highest bid, and winners received no excess refund. Claims now total the full charged history and subtract only the winning price for the winner. Refunds occur when claiming after the auction ends; this does not change bid-time charging or compensate claims completed before the fix.
6. **P0 — Claims lack durable atomic settlement (fixed).** Previously menu callbacks paid cached entitlements and relied on later independent profile writes. The service now stores a replayable, profile-bound receipt and validates current eligibility before reserving it. Delivery records and rewards persist atomically in the player profile, with local lifecycle coordination, persisted receipt checks and reconnect recovery after uncertain saves. Item claims require one empty inventory slot. Archive work must preserve the stored claim receipts unchanged.

## Reliability and browsing

7. **P1 — Request failures strand reserved funds.** BIN and normal bids deduct before requests and lack compensation on exceptional completion; response success is ignored. BIN also never checks fresh expiry and rewrites purchased listings into TOOLS.
8. **P1 — Large prices overflow after fees are deducted.** The GUI accepts long prices, but `AuctionItem` uses `Math.toIntExact` and Mongo/protocol fields are int. A 3-billion-coin listing deterministically throws in the probe.
9. **P1 — Filtered browsing crashes (fixed).** `AuctionsCacheService` returns `Stream.toList()` for BIN_ONLY/AUCTIONS_ONLY; `EndpointFetchItems` sorts that immutable list. Reproduced BIN_ONLY failure. SHOW_ALL sorting mutates a shared cached list.
10. **P1 — Expiry cleanup is racy.** Originally browsing separately inserted inactive and deleted active records, then returned expired documents anyway. Destructive read-time archival was removed with #32, but expired-result filtering and the archive lifecycle remain open. Changing filters within the cache window attempts to archive the same ID twice (reproduced); Mongo can reject the second insert. Expiry only runs when a category is read.
11. **P1 — Missing listing reports malformed success (fixed).** `EndpointFetchItem` returns an empty AuctionItem with null identity/item/seller for unknown IDs. Its serialization throws (reproduced), and GUI consumers have no not-found branch. If duplicate active/inactive IDs exist, the inactive copy wins.
12. **P1 — Browser hides results and lacks navigation.** `GUIAuctionBrowser` places N results then calls `fillInAir(N / 2)`, erasing half. One result disappears entirely; 24 results show only 12. Page remains 1 without navigation. Manage Auctions and View Bids also expose only their first seven entries without later-page controls.
13. **P1 — Profile switching is not coordinated with transactions.** Listings/bids identify accounts without profile identity. Async callbacks access the current data handler after network waits without checking the original profile or acquiring lifecycle transition locks. Bid tracking can land on another profile. Co-op checks use account membership, not the listing profile. Source-derived risk, not live-reproduced.
14. **P1 — Eligibility restrictions are missing.** Listing intake checks only air/unknown items, not soulbinding or Bazaar eligibility. Auction NPCs/transaction paths do not check Ironman despite its mode menu promising no Auction House. Service writes also do not enforce buyer/seller/co-op eligibility.

## Behavior and parity gaps

15. **P2 — Bid increment/timing.** Minimum is highest bid + 1; every bid adds two minutes regardless of time remaining; requests are accepted until five seconds after expiry. Hypixel's historical official update specifies percentage tiers. Verify current timing rules before implementing parity.
16. **P2 — Fees, cancellation and duration.** BIN and normal both charge 5% plus linear duration fees; seller collection takes no tax. Exact current fee rules need verification. Unbid/unsold cancellation is absent despite official support. Custom minutes bypass the maximum-duration check and multiplication can overflow.
17. **P2 — Sorts, freshness and statistics.** Price sorting uses starting price rather than current highest bid; MOST_BIDS is ascending. Periodic browser refresh redraws old cached results instead of fetching fresh listings. Async category/filter responses are not correlated with current selection. Stats are read for display, but no transaction updates were found.
18. **P2 — Notification links require staff.** Notifications run `/ahview`, while `AhViewCommand` requires STAFF; ordinary players cannot use the link.

## Code locations

- Service: `service.auctionhouse/src/main/java/dev/daweapon/service/auction/` — `endpoints/EndpointAddItem.java`, `EndpointFetchItem.java`, `EndpointFetchItems.java`, and `AuctionsCacheService.java`.
- Menus/transactions: `type.skyblockgeneric/src/main/java/dev/daweapon/type/skyblockgeneric/gui/inventories/auction/` and its `view/` directory.
- Data: `commons/src/main/java/dev/daweapon/commons/skyblock/auctions/AuctionItem.java` and `commons/src/main/java/dev/daweapon/commons/protocol/serializers/AuctionItemSerializer.java`.
- Entry/restrictions: `type.hub/.../npcs/NPCAuctionMaster.java`, `NPCAuctionAgent1.java`, `NPCAuctionAgent2.java`; `type.skyblockgeneric/.../commands/AhViewCommand.java`.

## Reproduction

From the repository root:

```sh
docker cp service_auctionhouse:/app/ServiceAuctionHouse.jar /tmp/auction-audit-service.jar
java -cp /tmp/auction-audit-service.jar tools/auction-audit/AuctionAuditProbe.java
```

The probe reports preserved item metadata, synchronous acknowledgement, filtered sorting and missing-ID handling as FIXED, archival as PARTIAL, and reproduces the remaining price-overflow failure. Once repaired, replace those expectations with regression tests for correct behavior. Reproduced cases: item mutation, premature acknowledgement, immutable filtered results, expiry/archive duplication attempts, missing-ID serialization and price overflow.

## Repair order

1. Preserve item identity; replace arbitrary listing replacement with atomic create/bid/buy operations and durable operation IDs.
2. Reserve money/items once; handle failures and uncertain responses; record profile ownership and settlement state. Test duplicate requests, two buyers, rebidding, profile switching and restart recovery.
3. Fix expiry/not-found handling, then browsing/navigation and access to all owned auctions/bids.
4. Verify and implement fees, restrictions, cancellation and bid rules; update stats and command permissions/documentation.
5. Test BIN, bidding, rebidding, expiration, cancellation and every claim path with isolated accounts before deployment.

## Hypixel references

[Official May 19, 2020 BIN/cancellation update](https://hypixel.net/threads/may-19-auction-house-bin-cancels.2909274/) documents fixed-price purchases, cancellation of unbid/unsold listings, percentage bid tiers and exclusion of Bazaar products. This historical primary source does not prove every numeric rule is unchanged today.

[Hypixel public API](https://api.hypixel.net/) documents auction records. Current taxes, fee thresholds and timing require primary-source verification before changing economics; third-party search results were not treated as definitive specifications.

## Follow-up review (2026-09-08)

See [auction-house-review.md](auction-house-review.md) for the review of the subsequent changes, compatibility and refund corrections, remaining recovery risks and validation. The Admin GUI is implemented independently; `/adminbypass` remains deferred.
