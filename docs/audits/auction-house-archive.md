# Auction expiry archive — #39

## Hypixel reference

The [Hypixel SkyBlock Wiki Auction House page](https://hypixelskyblock.minecraft.wiki/w/Auction_House) documents finished-auction claiming, seller collection, bidder refunds, and the Claim All flow. Ended listings therefore have to leave the public browser while remaining retrievable for those outcomes.

## Implemented behavior

- The auction service scans ended active listings every five seconds in bounded batches and moves them to inactive storage independently of browser traffic.
- Public browsing continues to filter ended listings immediately, including cached results.
- Archival is idempotent. Concurrent workers converge on one inactive document rather than creating duplicates.
- Claims, bid-profile bindings, seller-profile ownership, bid history, creation receipts, and all other immutable listing data are preserved.
- The active copy is deleted only when its end time, bids, claims, and profile bindings still match the archived snapshot. If a claim changes the listing during archival, deletion is refused and the newer state is copied on the next pass.
- A stale worker can add missing claim/profile entries but cannot erase entries already copied by a newer worker.

## Validation and installation

100 targeted auction tests passed with zero failures, errors, or skips. New disposable-MongoDB tests cover active-versus-ended selection, repeat archival, stale-worker claim preservation, and retrying a listing changed during archival.

The service jar was built with `--no-build-cache`, completely read as a ZIP, installed into the stopped auction-service container and persistent override, and read back with matching SHA256.

- ServiceAuctionHouse.jar: `c9500f008a2c38fec788e1d5a16f5f4b7754b5c5e1c8e3ae4c1ef38b61a672e3`
- Previous jar: `/home/bradl/.hypixel-skyblock/backups/auction-archive-20260908-141554`

The server stack remains stopped. In-game confirmation is pending.
