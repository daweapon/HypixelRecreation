# Auction statistics — #46

## Hypixel reference

Hypixel's Auction Stats menu separates seller and buyer lifetime totals. Historical in-game examples show seller fees, coins earned, auctions created and item sales, plus buyer bids, highest bid, auctions won, coins spent and items bought. The official Public API identifies the authoritative auction participants, profile IDs, final highest bid and bid history used to derive those outcomes.

- [Hypixel Public API auction schema](https://api.hypixel.net/)
- [Hypixel forum example of the Auction Stats menu](https://hypixel.net/threads/what-are-your-ah-stats.2597955/)

## Implemented behavior

- A confirmed listing increments auctions created and adds the exact charged listing fee. Creation retries cannot count twice because the update occurs while settling the persisted escrow.
- Each confirmed normal-auction bid increments total bids and updates highest bid made. BIN purchases do not increment bid count.
- Claiming a sold auction increments the seller's completed-with-bids count, earnings, highest auction and sold rarity. Claiming an unsold listing increments completed-without-bids.
- A winning buyer claim increments auctions won, final coins spent and bought rarity. Losing-bid refunds do not count as purchases.
- Claim receipts now persist the service-authoritative final sale price and original item. Statistics settle in the same profile transaction as the durable claim receipt, preventing duplicate increments after retries or lost acknowledgements.
- Coin statistics use 64-bit values and retain compatibility with existing integer-valued profile data.

## Validation and installation

97 targeted auction tests passed with zero failures, errors or skips, including disposable MongoDB and Redis integration tests. New cases cover creation fees, repeat-safe creation, normal bids versus BIN purchases, highest bids, seller and buyer claims, unsold claims, rarity totals and coin values above the 32-bit range.

Both jars were built with `--no-build-cache`, completely read as ZIP files, installed into the stopped auction service and eight stopped game containers, and read back with matching SHA256 hashes. Persistent overrides were updated as well.

- HypixelCore.jar: `4dd19095078880bb15e473c6c393166adb1a88048744a4c0cd6eb8cb32fd400e`
- ServiceAuctionHouse.jar: `c1a98cdd6a04546016bfce0d5d4ba66078ffa19ec9c66611ef824d029e4f8501`
- Previous jars: `/home/bradl/.hypixel-skyblock/backups/auction-stats-20260908-140759`

The server stack remains stopped. In-game confirmation is pending.
