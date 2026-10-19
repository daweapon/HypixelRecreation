# Auction profile ownership — #42

## Completed behavior

- New listings save the seller's original profile through BSON, JSON, and cloning. Creation retries preserve that identity and cannot replace it with another profile.
- The service checks that creation, bid, and claim profiles belong to the requesting account. Seller payouts and unsold items can only be claimed on the listing's original profile.
- Co-op restrictions use the seller's listing profile and the bidder's participating profile. A co-op on another profile of either account does not incorrectly block a solo-profile purchase. Service-side checks apply to both BIN and normal bids.
- Legacy bid RPCs without profile identity are rejected. Current clients use the durable profile-aware protocol introduced for #36.
- Pending creation retries automatically on profile load, opening the Auction House, and after an unresolved request. Switching profiles or disconnecting leaves the request on the original profile. Recovery does not create another listing, charge another fee, or close an unrelated menu.
- Listing views discard responses belonging to a closed menu or another profile, and prevent older responses replacing newer ones. Seller controls and auction lore distinguish a listing on another profile.

## Older listings

Listings and pending creation requests that predate seller-profile metadata remain readable. The service resolves old ownership using the account's profile list and exact saved owned/bid tracking or pending creation records. A unique result is bound when a bid or claim succeeds; the original creation receipt is preserved for retries.

Missing or ambiguous ownership is not guessed from the selected profile. Such historical records require review before purchase/claim. Existing immutable claim and bid receipts remain replayable on their bound profiles. No production auction/player records were edited during this repair.

## Validation

85 targeted tests passed with zero failures, errors, or skips. New checks cover original-profile seller payouts and unsold returns, BIN/normal co-op restrictions, unrelated co-op profiles, foreign-profile rejection, immutable listing identity, legacy ownership and pending-creation resolution, ambiguous legacy records, profile-storage failures, serialization/cloning, and creation reload recovery.

A service purchase-and-claim test uses the actual shared profile and co-op storage code against disposable Redis and MongoDB instances. Existing bid concurrency, lost acknowledgement, claim settlement, reservation, numeric compatibility, menu, and wipe-coverage tests also pass. The six-check isolated audit probe passed. Both jars were built with `--no-build-cache` and checked for full ZIP integrity.

The production stack is stopped. In-game menu and profile-switch validation remains pending when it is started. Other audit items, including broader browser pagination/freshness and archival, are separate from #42.

## Reproducing the tests

Use disposable local databases only:

```sh
AUCTION_TEST_MONGO_URI=mongodb://127.0.0.1:TEST_MONGO_PORT \
AUCTION_TEST_REDIS_URI=redis://127.0.0.1:TEST_REDIS_PORT \
./gradlew :service.auctionhouse:test :type.skyblockgeneric:test \
  --tests '*Auction*' --tests '*EndpointAddItemTest' --tests '*ProfileWipeCoverageTest' \
  --no-build-cache
```

The Mongo tests create and remove uniquely named databases. The Redis storage test writes random test accounts/profiles and rosters; destroy its disposable Redis instance after the test.

## Installation

Installed and SHA-256 verified in `service_auctionhouse`, all eight `hypixelcore_*` containers, and the persistent jar overrides. The production stack remained stopped.

- `HypixelCore.jar`: `6752ba92bbe0e43569d8715cd4c2958dc852512c4415e2b062af8eeda3913c26`
- `ServiceAuctionHouse.jar`: `4e1284eef421c897b401b00d4780210c438c2518bd87839b7559ed4e4c130ad5`

Previous jars are backed up at `/home/bradl/.hypixel-skyblock/backups/auction-profiles-20260908-133622`.
