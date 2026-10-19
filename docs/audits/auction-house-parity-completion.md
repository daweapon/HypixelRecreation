# Auction House parity completion — issues #43–45

## Hypixel reference

The remaining behavior was checked against the [Hypixel SkyBlock Wiki Auction House page](https://hypixelskyblock.minecraft.wiki/w/Auction_House): 2.5% minimum bid raises, the final-two-minute timer reset, the 4m45s browser delist window, the 20-second BIN purchase grace period, listing fee tiers, claim tax, duration fees and the 14-day cap. Soulbound and Bazaar-traded item exclusions follow the same wiki rule that those items cannot be listed on the Auction House.

## Implemented

- Auction eligibility rejects Ironman profiles, foreign profiles, the seller and participating co-op profiles, soulbound items, and items traded through the Bazaar. Checks run before escrow and again in the authoritative service where account/profile data is available.
- Normal bids require a 2.5% increase rounded up. A bid during the final two minutes resets the auction to two minutes remaining. Listings leave the browser 4m45s before expiry while direct auction views and valid final bids remain available.
- BIN listings cannot be purchased for their first 20 seconds.
- Prices support 1 through 50 billion coins. Durations support up to 336 hours.
- Regular listings charge 5%; BIN listings charge 1% below 10m, 2% from 10m to below 100m, and 2.5% from 100m upward. Duration charges use the wiki fee anchors from 20 coins at one hour through 55,200 coins at 14 days.
- Seller collection tax is 1% above one million coins, capped so proceeds never fall below one million.
- Owners can cancel active auctions with no bids and immediately collect the returned item. Auctions with a bid cannot be cancelled. The service validates ownership and profile identity atomically.

## Validation and installation

All 166 commons, auction service, and SkyBlock tests passed against disposable MongoDB and Redis, including concurrency, retry receipts, archival races, profile ownership, co-op restrictions, claims, statistics, fee calculations and cancellation. Both shadow jars were built with `--no-build-cache` and fully checked as ZIP archives.

- HypixelCore.jar: `69523429cd44d636233e74f774f6c31d30ff9b00ee2884216679a2475e0a6b6c`
- ServiceAuctionHouse.jar: `91ef64504e852b2f49fb214349c8403fe0d1ba80f7627e23542105702eb01451`
- Backup: `/home/bradl/.hypixel-skyblock/backups/auction-parity-20260908-143843`

The jars were installed into the persistent Docker build inputs, all eight game containers, and the auction service container. All read-back hashes matched.
