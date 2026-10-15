package dev.daweapon.service.auction.endpoints;

import com.mongodb.client.*;
import net.minestom.server.item.Material;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionBidProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionBidProtocol.*;
import dev.daweapon.commons.protocol.objects.auctions.AuctionClaimProtocol;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.service.auction.AuctionActiveDatabase;
import dev.daweapon.service.auction.AuctionInactiveDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AuctionBidRecoveryIntegrationTest {
    private MongoClient client;
    private MongoCollection<Document> previousActive, previousInactive;
    private String database;
    private final EndpointBid endpoint = new EndpointBid(new AuctionTestProfiles());

    @BeforeEach void setUp() {
        String uri = System.getenv("AUCTION_TEST_MONGO_URI");
        Assumptions.assumeTrue(uri != null, "Requires isolated MongoDB");
        client = MongoClients.create(uri);
        database = "auction_test_" + UUID.randomUUID().toString().replace("-", "");
        previousActive = AuctionActiveDatabase.collection;
        previousInactive = AuctionInactiveDatabase.collection;
        AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("active");
        AuctionInactiveDatabase.collection = client.getDatabase(database).getCollection("inactive");
    }
    @AfterEach void cleanUp() {
        if (client == null) return;
        client.getDatabase(database).drop(); client.close();
        AuctionActiveDatabase.collection = previousActive; AuctionInactiveDatabase.collection = previousInactive;
    }

    @Test void committedBinWithLostAcknowledgementRecoversWithoutSecondPurchase() {
        var auction = create(true);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 100);
        var real = AuctionActiveDatabase.collection;
        AuctionActiveDatabase.collection = failWrite(real, true);
        assertThrows(IllegalStateException.class, () -> endpoint.handle(request, null));
        AuctionActiveDatabase.collection = real;
        var result = endpoint.handle(wire(request), null);
        assertTrue(result.success());
        assertEquals(result, endpoint.handle(wire(request), null));
        assertEquals(1, document(auction).getList("bids", String.class).size());
        assertEquals("WEAPONS", document(auction).getString("category"));
    }

    @Test void requestLostBeforeWriteCanRetryWithoutFreshSnapshot() {
        var auction = create(false);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 120);
        var real = AuctionActiveDatabase.collection;
        AuctionActiveDatabase.collection = failWrite(real, false);
        assertThrows(IllegalStateException.class, () -> endpoint.handle(request, null));
        AuctionActiveDatabase.collection = real;
        assertTrue(document(auction).getList("bids", String.class).isEmpty());
        assertTrue(endpoint.handle(wire(request), null).success());
        assertEquals(1, document(auction).getList("bids", String.class).size());
    }

    @Test void simultaneousRetriesReturnOneStableAcceptance() throws Exception {
        var auction = create(false);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 120);
        var results = compete(Collections.nCopies(24, request));
        assertTrue(results.stream().allMatch(Response::success));
        assertEquals(1, results.stream().distinct().count());
        assertEquals(1, document(auction).getList("bids", String.class).size());
    }

    @Test void competingBuyersGetDurableRejectionsAndOneAcceptance() throws Exception {
        var auction = create(true);
        var requests = new ArrayList<Request>();
        for (int i = 0; i < 24; i++) requests.add(request(auction, UUID.randomUUID(), UUID.randomUUID(), 100));
        var results = compete(requests);
        assertEquals(1, results.stream().filter(Response::success).count());
        for (int i = 0; i < requests.size(); i++) assertEquals(results.get(i), endpoint.handle(wire(requests.get(i)), null));
        assertEquals(24, document(auction).get("bid_operations", Document.class).size());
        assertEquals(1, document(auction).getList("bids", String.class).size());
    }

    @Test void rejectionCannotLaterCommitEvenIfListingStateIsRestored() {
        var auction = create(true);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 100);
        AuctionActiveDatabase.collection.updateOne(query(auction), new Document("$set", new Document("end", 1L)));
        var rejection = endpoint.handle(request, null);
        assertFalse(rejection.success());
        AuctionActiveDatabase.collection.updateOne(query(auction), new Document("$set", new Document("end", auction.getEndTime())));
        assertEquals(rejection, endpoint.handle(wire(request), null));
        assertTrue(document(auction).getList("bids", String.class).isEmpty());
    }

    @Test void replayAfterLaterBidAndArchivalRetainsOriginalOutcome() {
        var auction = create(false);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 120);
        var first = endpoint.handle(request, null);
        assertTrue(first.success());
        var latest = AuctionItem.fromDocument(document(auction));
        assertTrue(endpoint.handle(request(latest, UUID.randomUUID(), UUID.randomUUID(), 140), null).success());
        AuctionInactiveDatabase.collection.insertOne(document(auction));
        AuctionActiveDatabase.collection.deleteOne(query(auction));
        assertEquals(first, endpoint.handle(wire(request), null));
    }

    @Test void operationIdCannotBeReusedForDifferentAmountOrProfile() {
        var auction = create(false);
        var player = UUID.randomUUID(); var profile = UUID.randomUUID();
        var request = request(auction, player, profile, 120);
        assertTrue(endpoint.handle(request, null).success());
        var altered = request(auction, player, profile, 130);
        assertThrows(IllegalArgumentException.class, () -> endpoint.handle(new Request(request.operation(), player, profile, altered.bid()), null));
        assertThrows(IllegalArgumentException.class, () -> endpoint.handle(new Request(request.operation(), player, UUID.randomUUID(), request.bid()), null));
    }

    @Test void bidderCannotMoveBidsOrClaimsToAnotherProfile() {
        var auction = create(false);
        var player = UUID.randomUUID(); var profile = UUID.randomUUID();
        assertTrue(endpoint.handle(request(auction, player, profile, 120), null).success());
        assertTrue(endpoint.handle(request(AuctionItem.fromDocument(document(auction)), UUID.randomUUID(), UUID.randomUUID(), 140), null).success());
        assertFalse(endpoint.handle(request(AuctionItem.fromDocument(document(auction)), player, UUID.randomUUID(), 160), null).success());
        assertTrue(endpoint.handle(request(AuctionItem.fromDocument(document(auction)), player, profile, 160), null).success());
        AuctionActiveDatabase.collection.updateOne(query(auction), new Document("$set", new Document("end", 1L)));
        var claims = new EndpointClaim(new AuctionTestProfiles());
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(auction.getUuid(), player, UUID.randomUUID()), null).success());
        var claim = claims.handle(new AuctionClaimProtocol.Request(auction.getUuid(), player, profile), null);
        assertTrue(claim.success());
        assertEquals(120, claim.receipt().coins());
        assertNotNull(claim.receipt().item());
    }

    @Test void missingListingIsUnknownInsteadOfUnsafeRefundAuthorization() {
        var auction = create(true);
        var request = request(auction, UUID.randomUUID(), UUID.randomUUID(), 100);
        AuctionActiveDatabase.collection.deleteOne(query(auction));
        assertThrows(IllegalStateException.class, () -> endpoint.handle(request, null));
    }

    private AuctionItem create(boolean bin) {
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 600_000, bin, 100L);
        assertTrue(new EndpointAddItem(new AuctionTestProfiles()).handle(new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.WEAPONS), null).success());
        AuctionActiveDatabase.collection.updateOne(query(item), new Document("$set",
                new Document("created_at", System.currentTimeMillis() - 21_000)));
        return item;
    }
    private Request request(AuctionItem auction, UUID player, UUID profile, long amount) {
        var expected = AuctionAddItemProtocol.ExpectedState.of(auction);
        var copy = AuctionItem.fromDocument(auction.toDocument());
        copy.setBids(new ArrayList<>(copy.getBids()));
        copy.getBids().add(new AuctionItem.Bid(System.currentTimeMillis(), player, amount));
        return new Request(UUID.randomUUID(), player, profile, new AuctionAddItemProtocol.AuctionAddItemMessage(copy, AuctionCategories.TOOLS, expected));
    }
    private Document query(AuctionItem auction) { return new Document("_id", auction.getUuid().toString()); }
    private Document document(AuctionItem auction) { return AuctionActiveDatabase.collection.find(query(auction)).first(); }
    private Request wire(Request request) {
        var serializer = new AuctionBidProtocol().getSerializer();
        return serializer.deserialize(serializer.serialize(request));
    }
    private List<Response> compete(List<Request> requests) throws Exception {
        var start = new CountDownLatch(1);
        var tasks = new ArrayList<Future<Response>>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var request : requests) tasks.add(executor.submit(() -> { start.await(); return endpoint.handle(wire(request), null); }));
            start.countDown();
            var responses = new ArrayList<Response>();
            for (var task : tasks) responses.add(task.get(15, TimeUnit.SECONDS));
            return responses;
        }
    }
    @SuppressWarnings("unchecked")
    private MongoCollection<Document> failWrite(MongoCollection<Document> real, boolean afterCommit) {
        var once = new AtomicBoolean();
        return (MongoCollection<Document>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MongoCollection.class}, (p, m, a) -> {
            boolean fail = m.getName().equals("updateOne") && once.compareAndSet(false, true);
            if (fail && !afterCommit) throw new IllegalStateException("Simulated request lost before write");
            try {
                Object result = m.invoke(real, a);
                if (fail) throw new IllegalStateException("Simulated acknowledgement lost after write");
                return result;
            } catch (InvocationTargetException exception) { throw exception.getCause(); }
        });
    }
}
