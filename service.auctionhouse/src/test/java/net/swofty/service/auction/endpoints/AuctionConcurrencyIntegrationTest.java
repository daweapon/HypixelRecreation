package net.swofty.service.auction.endpoints;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol.*;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.service.auction.AuctionActiveDatabase;
import net.swofty.service.auction.AuctionsCacheService;
import net.swofty.commons.skyblock.auctions.AuctionsFilter;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class AuctionConcurrencyIntegrationTest {
    private MongoClient client;
    private MongoCollection<Document> previous;
    private String database;

    @BeforeEach
    void setUp() {
        String uri = System.getenv("AUCTION_TEST_MONGO_URI");
        Assumptions.assumeTrue(uri != null, "Requires isolated MongoDB via AUCTION_TEST_MONGO_URI");
        client = MongoClients.create(uri);
        database = "auction_test_" + UUID.randomUUID().toString().replace("-", "");
        previous = AuctionActiveDatabase.collection;
        AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("auctions");
    }

    @AfterEach
    void cleanUp() {
        if (client == null) return;
        client.getDatabase(database).drop();
        AuctionActiveDatabase.collection = previous;
        client.close();
    }

    @Test
    void competingBinBuyersHaveExactlyOneWinner() throws Exception {
        var auction = create(true);
        String originalItem = document(auction).getString("item");
        var results = compete(auction, 100);
        assertEquals(1, results.stream().filter(AuctionAddItemResponse::success).count());
        var saved = document(auction);
        assertEquals(1, saved.getList("bids", String.class).size());
        assertEquals(originalItem, saved.getString("item"));
        assertEquals("WEAPONS", saved.getString("category"));
    }

    @Test
    void competingBidsRejectStaleStateAndFreshBidPreservesHistory() throws Exception {
        var auction = create(false);
        assertEquals(1, compete(auction, 110).stream().filter(AuctionAddItemResponse::success).count());
        var first = document(auction);
        var current = AuctionItem.fromDocument(first);
        assertTrue(handle(bid(current, UUID.randomUUID(), 120), null).success());
        var saved = document(auction);
        assertEquals(2, saved.getList("bids", String.class).size());
        assertEquals(first.getList("bids", String.class).getFirst(), saved.getList("bids", String.class).getFirst());
        assertFalse(handle(bid(auction, UUID.randomUUID(), 1000), null).success());
    }

    @Test
    void cannotOverwriteExistingListingThroughCreateOrBidPayload() {
        var auction = create(true);
        String item = document(auction).getString("item");
        assertFalse(handle(new AuctionAddItemMessage(auction, AuctionCategories.TOOLS), null).success());
        var update = bid(auction, UUID.randomUUID(), 100);
        update.item().setItem(new UnderstandableSkyBlockItem(null, List.of(), 64, Material.DIAMOND));
        assertTrue(handle(update, null).success());
        assertEquals(item, document(auction).getString("item"));
        assertEquals("WEAPONS", document(auction).getString("category"));
    }

    @Test
    void rejectsExpiredOwnAndUnderpricedPurchases() {
        var auction = create(true);
        assertFalse(handle(bid(auction, auction.getOriginator(), 100), null).success());
        assertFalse(handle(bid(auction, UUID.randomUUID(), 99), null).success());
        AuctionActiveDatabase.collection.updateOne(new Document("_id", auction.getUuid().toString()),
                new Document("$set", new Document("end", System.currentTimeMillis() - 1)));
        var expired = AuctionItem.fromDocument(document(auction));
        assertFalse(handle(bid(expired, UUID.randomUUID(), 100), null).success());
    }

    @Test
    void browsingAnOldCachedSnapshotCannotDeleteAnAcceptedBid() {
        var auction = create(false);
        var cache = new AuctionsCacheService();
        cache.getDocuments("WEAPONS");
        assertTrue(handle(bid(auction, UUID.randomUUID(), 110), null).success());
        cache.getDocuments("WEAPONS").getFirst().put("end", System.currentTimeMillis() - 1);
        cache.getAuctions("WEAPONS", AuctionsFilter.SHOW_ALL);
        assertEquals(1, document(auction).getList("bids", String.class).size());
    }

    @Test
    void expectedStateSurvivesWireSerialization() {
        var message = bid(create(false), UUID.randomUUID(), 110);
        var serializer = new AuctionAddItemProtocol().getSerializer();
        var roundTrip = serializer.deserialize(serializer.serialize(message));
        assertEquals(message.expected(), roundTrip.expected());
        assertTrue(handle(roundTrip, null).success());
    }

    @Test
    void creationRetryStillSucceedsAfterSaleWithoutOverwritingIt() {
        var auction = create(true);
        var request = new AuctionAddItemMessage(auction, AuctionCategories.WEAPONS);
        assertTrue(handle(request, null).success());
        assertTrue(handle(bid(auction, UUID.randomUUID(), 100), null).success());
        var sold = document(auction);
        assertTrue(handle(request, null).success());
        assertEquals(sold, document(auction));
        assertEquals(1, AuctionActiveDatabase.collection.countDocuments());
    }

    @Test
    void concurrentCreationRetriesPersistExactlyOneListing() throws Exception {
        var auction = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 3, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 600000, true, 100L);
        var message = new AuctionAddItemMessage(auction, AuctionCategories.WEAPONS);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = new CountDownLatch(1);
            var futures = new ArrayList<Future<AuctionAddItemResponse>>();
            for (int i = 0; i < 24; i++) futures.add(executor.submit(() -> {
                start.await(); return handle(message, null);
            }));
            start.countDown();
            for (var future : futures) assertTrue(future.get(10, TimeUnit.SECONDS).success());
        }
        assertEquals(1, AuctionActiveDatabase.collection.countDocuments());
        assertTrue(document(auction).getList("bids", String.class).isEmpty());
    }

    @Test
    void delayedCreationCanBeRecoveredAfterItsEndTime() {
        var auction = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 3, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() - 600000, true, 100L);
        var request = new AuctionAddItemMessage(auction, AuctionCategories.WEAPONS);
        assertTrue(handle(request, null).success());
        assertTrue(handle(request, null).success());
        assertEquals(1, AuctionActiveDatabase.collection.countDocuments());
        assertFalse(handle(bid(auction, UUID.randomUUID(), 100), null).success());
    }

    @Test
    void legacyIntegerCreationReceiptStillAcknowledgesLongPriceRetry() {
        var item = create(true);
        var original = item.toDocument().append("category", "WEAPONS");
        original.put("starting-price", 100);
        AuctionActiveDatabase.collection.updateOne(new Document("_id", item.getUuid().toString()),
                new Document("$set", new Document("starting-price", 100).append("creation", original)));
        assertTrue(handle(new AuctionAddItemMessage(item, AuctionCategories.WEAPONS), null).success());
        assertEquals(1, AuctionActiveDatabase.collection.countDocuments());
    }

    private AuctionAddItemResponse handle(AuctionAddItemMessage message, net.swofty.commons.redis.RedisMessageContext context) {
        if (message.expected() == null) return new EndpointAddItem(new AuctionTestProfiles()).handle(message, context);
        UUID player = message.item().getBids().getLast().uuid();
        var result = new EndpointBid(new AuctionTestProfiles()).handle(new net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol.Request(
                UUID.randomUUID(), player, AuctionTestProfiles.profile(player), message), context);
        return new AuctionAddItemResponse(result.auction(), result.success(), result.error());
    }

    private AuctionItem create(boolean bin) {
        var auction = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 3, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 600_000, bin, 100L);
        assertTrue(handle(new AuctionAddItemMessage(auction, AuctionCategories.WEAPONS), null).success());
        AuctionActiveDatabase.collection.updateOne(new Document("_id", auction.getUuid().toString()),
                new Document("$set", new Document("created_at", System.currentTimeMillis() - 21_000)));
        assertNotNull(document(auction));
        return auction;
    }

    private Document document(AuctionItem auction) {
        return AuctionActiveDatabase.collection.find(new Document("_id", auction.getUuid().toString())).first();
    }

    private AuctionAddItemMessage bid(AuctionItem original, UUID bidder, long value) {
        var proposed = AuctionItem.fromDocument(original.toDocument());
        var expected = ExpectedState.of(original);
        var bids = new ArrayList<>(original.getBids());
        bids.add(new AuctionItem.Bid(System.currentTimeMillis(), bidder, value));
        proposed.setBids(bids);
        return new AuctionAddItemMessage(proposed, AuctionCategories.TOOLS, expected);
    }

    private List<AuctionAddItemResponse> compete(AuctionItem original, long value) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = new CountDownLatch(1);
            var futures = new ArrayList<Future<AuctionAddItemResponse>>();
            for (int i = 0; i < 24; i++) {
                var request = bid(original, UUID.randomUUID(), value);
                futures.add(executor.submit(() -> { start.await(); return handle(request, null); }));
            }
            start.countDown();
            var results = new ArrayList<AuctionAddItemResponse>();
            for (var future : futures) results.add(future.get(10, TimeUnit.SECONDS));
            return results;
        }
    }
}
