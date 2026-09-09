# Auction pagination — #41

## Completed behavior

- Auction Browser has 24 listings per page; Manage Auctions and View Bids have seven. Previous/Next arrows and a page indicator make later results reachable. Unavailable directions are disabled.
- Inspecting an auction and returning preserves the current page. Changing category, sorting, or BIN/auction filter resets to page one and removes the previous selection's results.
- Refreshes preserve the selected page where possible and clamp it when the result list shrinks. Unused slots are cleared, including partial final pages. Empty/loading/error states retain navigation and other controls.
- Personal lists resolve distinct tracked auction IDs, omit confirmed missing listings, and use stable end-time/UUID ordering. A failed refresh retains the last complete snapshot rather than silently dropping auctions from a partially failed batch.
- All three menus share paging and asynchronous refresh handling. Updates apply on the player's scheduler under profile lifecycle coordination, and check the active GUI identity as well as the inventory and profile. This matters because different menus can reuse the same Inventory object.

## Related #46 progress

The browser now fetches current listings on refresh, rather than redrawing its old cache. Selection changes invalidate old responses; closed-menu and previous-profile responses are ignored. Regular refreshes allow one current request at a time, with a timeout and automatic retry. The refresh interval is two seconds.

Statistics updates are now implemented under #46; see [the statistics report](auction-house-statistics.md). This pagination change does not complete the other remaining auction audit items.

## Validation

92 targeted tests passed with zero failures, errors, or skips. Seven new pagination scenarios cover reaching all 49 browser listings, seven-item pages and partial final pages, removing/collecting entries, empty results, resetting selections, stale/duplicate responses, returning to the same page, and immutable snapshots. Existing auction transaction, claim, ownership, and real disposable Redis/Mongo storage tests also pass.

Both jars were built with `--no-build-cache` and checked for complete ZIP integrity. No production player balances, inventories, or auction records were edited. The production servers remain stopped; in-game navigation verification is pending.

## In-game check when servers are running

Use more than 24 results in a browser category and more than seven tracked auctions/bids. Visit the last page, inspect an item and go back, then change category/filter. Collect an item from a personal list's final page and verify the remaining results and page count update correctly.

## Installation

Installed and read-back SHA256 verified in the auction service and all eight stopped game containers, plus persistent jar overrides. Servers were left stopped.

- HypixelCore.jar: `e50199179cef9b248cf4f0aa9c8c6922d34c6b96b6f38cb727da3639e616978c`
- ServiceAuctionHouse.jar: `d1ef62b5cfd8b2f7dc75f34d4b7935acd693d5de39e0e6c98dde5478744e4124`
- Previous jars backed up at `/home/bradl/.hypixel-skyblock/backups/auction-pages-20260908-135358`.
