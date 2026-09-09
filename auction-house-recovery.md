# Auction bid and BIN recovery — 2026-09-08

## Behavior

New bid and BIN requests reserve coins and save the exact request on the original profile in one profile transaction before contacting the auction service. A profile can have one unresolved bid/purchase at a time. A request that cannot fetch the listing reserves no coins.

The service saves a permanent accepted/rejected receipt in the same Mongo update as any bid change. Retries reuse the operation ID and exact expected listing state. An accepted operation stays accepted after subsequent bids, expiry, or receipt-preserving archival. A rejected operation cannot later become accepted through a delayed duplicate. A reused operation ID with different transaction inputs is treated as an error, never as refund authorization.

On acceptance, a second profile transaction clears the pending request and adds the auction to View Bids. On rejection, that transaction clears the pending request and returns the reserved coins. Lost responses leave the durable reservation intact for another check. Recovery runs on profile load, opening the Auction House, and after a 20-second delay following an unresolved request. Switching profiles or disconnecting leaves recovery attached to the original profile; return to that profile to finish it.

Bid participation and claims for new operations are bound to the original profile on the service. Existing claim completion is respected, so a late bid acknowledgement cannot reactivate a collected auction. Notifications happen separately from financial settlement.

## Storage and compatibility

- Private profile field `auction_operations`: revision and serialized pending request. It is saved atomically with coins, bid tracking, and the profile document. Older profiles default to no pending operation.
- Auction document fields `bid_operations` and `bid_profiles`: permanent operation receipts and bidder/profile bindings. Request fingerprints cover the transaction inputs without copying full item metadata into every receipt.
- Profile snapshots and transfers cannot erase a newer operation revision. Ambiguous profile save failures stop the player session for reload.
- New `AuctionBidProtocol` is separate from legacy listing creation/update RPCs. Both core and auction service jars must be installed together.

This provides recovery for requests created by the new code. Old stranded reservations without a persisted operation cannot be reconstructed automatically. A listing missing from both auction collections remains an unknown outcome rather than authorizing an unsafe refund; archival must preserve receipts. The remaining #42 ownership/co-op profile identity work is now implemented; see [the ownership report](auction-house-profile-ownership.md).

## Validation

71 targeted tests passed without failures, errors, or skips against a disposable Mongo instance and isolated client/profile tests. Added coverage includes writes lost before commit, acknowledgements lost after commit, 24 concurrent retries, 24 competing buyers, permanent rejection, receipt replay after later bids and archival, operation-ID reuse, cross-profile bids/claims, reservation reload, exactly-once refunds, stale response rejection, stale profile snapshot rejection, and late acknowledgements after collection.

The six-check isolated audit probe also passed. Builds use `--no-build-cache`; complete jar integrity and installation hashes are verified before deployment is recorded. The production server stack is stopped, so live bid/BIN/profile-switch testing remains pending.

## In-game verification when the stack is running

1. Place a normal bid and buy a BIN; confirm coins are charged once and the auction appears in View Bids.
2. With a second player, outbid the first and collect the finished auction/refunds.
3. Switch profiles or reconnect while a purchase is processing; return to the original profile and confirm the result recovers there.
4. In a test environment, interrupt auction-service responses and restore them; check that recovery produces either one accepted bid or one refund, never both.

## Installation

Installed and SHA-256 verified in `service_auctionhouse`, all eight `hypixelcore_*` containers, and the persistent jar overrides. The production stack remained stopped.

- `HypixelCore.jar`: `0e0be2d3ef26553000ec2f17bb65cb40fdc1ece088224653111d6a742854a98f`
- `ServiceAuctionHouse.jar`: `5e43f3f18685dd6233bdbc85aa3f54bb8a943d82b1820bc261345714d8b07791`

Previous jars are backed up at `/home/bradl/.hypixel-skyblock/backups/auction-recovery-20260908-130505`. No production balances, inventories, or auction documents were edited.
