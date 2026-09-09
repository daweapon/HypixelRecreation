# Auction audit probe

See [the audit report](../../auction-house-audit.md). `AuctionAuditProbe.java` invokes auction service classes with in-memory fake Mongo collections to check item preservation and reproduce the remaining reported service defects. It does not connect to any external database, spend coins or alter live listings.

```sh
docker cp service_auctionhouse:/app/ServiceAuctionHouse.jar /tmp/auction-audit-service.jar
java -cp /tmp/auction-audit-service.jar tools/auction-audit/AuctionAuditProbe.java
```

The item-type, write-acknowledgement, filtered-sorting and missing-ID checks report their corrected behavior. Browsing now reports partial expiry progress. Other assertions still describe faulty audited behavior, not acceptance criteria for a repaired service. Replace them with expectations for correct behavior when implementing fixes.

Concurrency integration tests run against a disposable MongoDB instance (never point this at production):

```sh
AUCTION_TEST_MONGO_URI=mongodb://127.0.0.1:TEST_PORT ./gradlew :service.auctionhouse:test --tests "*AuctionConcurrencyIntegrationTest"
```

The tests create and drop uniquely named `auction_test_...` databases and verify real MongoDB conditional updates, including 24 simultaneous contenders.

Claim integration tests use the same isolated Mongo URI with `--tests "*AuctionClaimsIntegrationTest"`. They cover concurrent receipt creation, profile binding, seller/buyer/refund eligibility and retry serialization. `AuctionClaimsTest` simulates interrupted delivery before/after commit; `AuctionClaimPersistenceTest` verifies transactional profile writes and rejection of stale or incomplete claim snapshots.

The complete targeted auction suite now also requires `AUCTION_TEST_REDIS_URI` pointing to a disposable Redis instance for the real profile/co-op storage test. See [auction-house-profile-ownership.md](../../auction-house-profile-ownership.md) for the command and current results.
