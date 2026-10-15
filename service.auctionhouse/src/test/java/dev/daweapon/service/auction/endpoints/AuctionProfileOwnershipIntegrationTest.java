package dev.daweapon.service.auction.endpoints;

import com.mongodb.client.*;
import net.minestom.server.item.Material;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionBidProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionClaimProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionCancelProtocol;
import dev.daweapon.commons.protocol.serializers.AuctionItemSerializer;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.service.auction.*;
import org.bson.Document;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionProfileOwnershipIntegrationTest {
    private MongoClient client;
    private String database;
    private MongoCollection<Document> active, inactive;
    private final Profiles profiles = new Profiles();
    private final EndpointAddItem create = new EndpointAddItem(profiles);
    private final EndpointBid bids = new EndpointBid(profiles);
    private final EndpointClaim claims = new EndpointClaim(profiles);
    private final EndpointCancel cancellation = new EndpointCancel(profiles);
    private final UUID seller = UUID.randomUUID(), sellerProfile = UUID.randomUUID(), sellerOther = UUID.randomUUID();
    private final UUID buyer = UUID.randomUUID(), buyerProfile = UUID.randomUUID(), buyerOther = UUID.randomUUID();

    @BeforeEach void setup() {
        String uri = System.getenv("AUCTION_TEST_MONGO_URI"); Assumptions.assumeTrue(uri != null);
        client = MongoClients.create(uri); database = "auction_test_" + UUID.randomUUID().toString().replace("-", "");
        active = AuctionActiveDatabase.collection; inactive = AuctionInactiveDatabase.collection;
        AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("active");
        AuctionInactiveDatabase.collection = client.getDatabase(database).getCollection("inactive");
        profiles.accounts.put(seller, List.of(sellerProfile, sellerOther));
        profiles.accounts.put(buyer, List.of(buyerProfile, buyerOther));
    }
    @AfterEach void cleanup() {
        if (client == null) return;
        client.getDatabase(database).drop(); client.close();
        AuctionActiveDatabase.collection = active; AuctionInactiveDatabase.collection = inactive;
    }

    @Test void soldListingPaysOnlyOriginalSellerAndBuyerProfiles() {
        var listing = listing(true);
        var request = bid(listing, buyerProfile);
        assertTrue(bids.handle(request, null).success());
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerOther), null).success());
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), buyer, buyerOther), null).success());
        var payment = claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerProfile), null);
        var item = claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), buyer, buyerProfile), null);
        assertTrue(payment.success()); assertEquals(100, payment.receipt().coins()); assertNull(payment.receipt().item());
        assertTrue(item.success()); assertNotNull(item.receipt().item()); assertEquals(0, item.receipt().coins());
        assertEquals(payment, claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerProfile), null));
        assertEquals(item, claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), buyer, buyerProfile), null));
    }

    @Test void ownerCanCancelOnlyBeforeTheFirstBid() {
        var cancellable = listing(false);
        assertFalse(cancellation.handle(new AuctionCancelProtocol.Request(cancellable.getUuid(), buyer, buyerProfile), null).success());
        assertTrue(cancellation.handle(new AuctionCancelProtocol.Request(cancellable.getUuid(), seller, sellerProfile), null).success());
        assertTrue(cancellation.handle(new AuctionCancelProtocol.Request(cancellable.getUuid(), seller, sellerProfile), null).success());
        assertTrue(document(cancellable).getLong("end") <= System.currentTimeMillis());

        var bidListing = listing(false);
        assertTrue(bids.handle(bid(bidListing, buyerProfile), null).success());
        assertFalse(cancellation.handle(new AuctionCancelProtocol.Request(bidListing.getUuid(), seller, sellerProfile), null).success());
    }

    @Test void coopRestrictionUsesTheParticipatingProfilesForBothBidTypes() {
        UUID coop = UUID.randomUUID();
        profiles.coops.put(sellerProfile, coop); profiles.coops.put(buyerProfile, coop);
        // The other profile of the same buyer is solo; account membership must not block it.
        for (boolean bin : List.of(true, false)) {
            var listing = listing(bin);
            assertFalse(bids.handle(bid(listing, buyerProfile), null).success());
            assertTrue(bids.handle(bid(listing, buyerOther), null).success());
        }
    }

    @Test void sellersUnrelatedCoopProfileDoesNotAffectOriginalSoloListing() {
        UUID coop = UUID.randomUUID();
        profiles.coops.put(sellerOther, coop); profiles.coops.put(buyerProfile, coop);
        assertTrue(bids.handle(bid(listing(true), buyerProfile), null).success());
    }

    @Test void foreignProfilesCannotCreateBidOrClaim() {
        var item = item(true); item.setSellerProfile(buyerProfile);
        assertFalse(create.handle(creation(item), null).success());
        var listing = listing(true);
        assertFalse(bids.handle(bid(listing, sellerOther), null).success());
        expire(listing);
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, buyerProfile), null).success());
        assertTrue(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerProfile), null).success());
    }

    @Test void listingProfileCannotBeChangedThroughCreationRetryOrBidPayload() {
        var listing = listing(true);
        var original = creation(listing);
        var changed = AuctionItem.fromDocument(listing.toDocument()); changed.setSellerProfile(sellerOther);
        assertFalse(create.handle(creation(changed), null).success());
        var request = bid(changed, buyerProfile);
        assertTrue(bids.handle(request, null).success());
        assertEquals(sellerProfile.toString(), document(listing).getString("seller_profile"));
        assertTrue(create.handle(original, null).success());
        assertEquals(1, document(listing).getList("bids", String.class).size());
    }

    @Test void legacyTrackingResolvesOriginalSellerAndPreservesCreationReceipt() {
        var listing = item(true); listing.setSellerProfile(null);
        var original = creation(listing); assertTrue(create.handle(original, null).success());
        backdate(listing);
        profiles.documents.put(sellerProfile, new Document("auction_owned", listing.getUuid().toString()));
        assertTrue(bids.handle(bid(listing, buyerProfile), null).success());
        assertEquals(sellerProfile.toString(), document(listing).getString("seller_profile"));
        assertTrue(create.handle(original, null).success());
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerOther), null).success());
        assertTrue(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerProfile), null).success());
    }

    @Test void unknownOrAmbiguousLegacyOwnershipIsNotAssignedToSelectedProfile() {
        var listing = item(true); listing.setSellerProfile(null); assertTrue(create.handle(creation(listing), null).success());
        assertFalse(bids.handle(bid(listing, buyerProfile), null).success());
        for (UUID profile : List.of(sellerProfile, sellerOther)) profiles.documents.put(profile,
                new Document("auction_owned", listing.getUuid().toString()));
        assertFalse(bids.handle(bid(listing, buyerProfile), null).success());
        expire(listing);
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), seller, sellerOther), null).success());
        assertTrue(document(listing).getList("bids", String.class).isEmpty());
        assertNull(document(listing).get("claims"));
    }

    @Test void pendingLegacyCreationIdentifiesOriginalProfileBeforeOwnershipTrackingSettles() {
        var listing = item(true); listing.setSellerProfile(null); var request = creation(listing);
        assertTrue(create.handle(request, null).success());
        backdate(listing);
        profiles.documents.put(sellerProfile, new Document("auction_escrow", new JSONObject().put("pendingCreation",
                new AuctionAddItemProtocol().getSerializer().serialize(request)).toString()));
        assertTrue(bids.handle(bid(listing, buyerProfile), null).success());
        assertEquals(sellerProfile.toString(), document(listing).getString("seller_profile"));
    }

    @Test void legacyBidClaimUsesExactTrackingAndBindsTheReceipt() {
        var listing = listing(false);
        AuctionActiveDatabase.collection.updateOne(new Document("_id", listing.getUuid().toString()), new Document("$set",
                new Document("bids", List.of(new AuctionItem.Bid(1L, buyer, 100L).toString())).append("end", 1L)));
        profiles.documents.put(buyerProfile, new Document("auction_bids", listing.getUuid().toString()));
        assertFalse(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), buyer, buyerOther), null).success());
        assertTrue(claims.handle(new AuctionClaimProtocol.Request(listing.getUuid(), buyer, buyerProfile), null).success());
        assertEquals(buyerProfile.toString(), document(listing).get("bid_profiles", Document.class).getString(buyer.toString()));
    }

    @Test void legacyBidEndpointCannotBypassProfileAndCoopChecks() {
        var listing = listing(true);
        assertFalse(create.handle(bid(listing, buyerProfile).bid(), null).success());
        assertTrue(document(listing).getList("bids", String.class).isEmpty());
    }

    @Test void profileLookupFailureLeavesOperationRetryable() {
        var listing = listing(true); var request = bid(listing, buyerProfile);
        profiles.unavailable = true;
        assertThrows(IllegalStateException.class, () -> bids.handle(request, null));
        assertNull(document(listing).get("bid_operations"));
        profiles.unavailable = false;
        assertTrue(bids.handle(request, null).success());
    }

    @Test void sellerIdentitySurvivesMongoWireAndCloneIncludingOldListings() {
        var item = item(false);
        item.setBids(List.of(new AuctionItem.Bid(1L, buyer, 100L)));
        var serializer = new AuctionItemSerializer<>();
        for (var copy : List.of(AuctionItem.fromDocument(item.toDocument()), serializer.deserialize(serializer.serialize(item)), serializer.clone(item))) {
            assertEquals(sellerProfile, copy.getSellerProfile()); assertEquals(item.getUuid(), copy.getUuid()); assertEquals(item.getBids(), copy.getBids());
        }
        item.setSellerProfile(null);
        assertNull(serializer.deserialize(serializer.serialize(item)).getSellerProfile());
        assertNull(AuctionItem.fromDocument(item.toDocument()).getSellerProfile());
    }

    private AuctionItem item(boolean bin) {
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE), seller,
                System.currentTimeMillis() + 600_000, bin, 100L);
        item.setSellerProfile(sellerProfile); return item;
    }
    private AuctionItem listing(boolean bin) {
        var item = item(bin); assertTrue(create.handle(creation(item), null).success());
        AuctionActiveDatabase.collection.updateOne(new Document("_id", item.getUuid().toString()),
                new Document("$set", new Document("created_at", System.currentTimeMillis() - 21_000)));
        return item;
    }
    private AuctionAddItemProtocol.AuctionAddItemMessage creation(AuctionItem item) {
        return new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.WEAPONS);
    }
    private AuctionBidProtocol.Request bid(AuctionItem listing, UUID profile) {
        var copy = AuctionItem.fromDocument(listing.toDocument());
        var expected = AuctionAddItemProtocol.ExpectedState.of(copy);
        copy.setBids(new ArrayList<>(copy.getBids())); copy.getBids().add(new AuctionItem.Bid(System.currentTimeMillis(), buyer, 100L));
        return new AuctionBidProtocol.Request(UUID.randomUUID(), buyer, profile,
                new AuctionAddItemProtocol.AuctionAddItemMessage(copy, AuctionCategories.TOOLS, expected));
    }
    private Document document(AuctionItem item) { return AuctionActiveDatabase.collection.find(new Document("_id", item.getUuid().toString())).first(); }
    private void expire(AuctionItem item) {
        AuctionActiveDatabase.collection.updateOne(new Document("_id", item.getUuid().toString()), new Document("$set", new Document("end", 1L)));
    }
    private void backdate(AuctionItem item) {
        AuctionActiveDatabase.collection.updateOne(new Document("_id", item.getUuid().toString()),
                new Document("$set", new Document("created_at", System.currentTimeMillis() - 21_000)));
    }
    private static final class Profiles extends AuctionProfiles {
        final Map<UUID, List<UUID>> accounts = new HashMap<>();
        final Map<UUID, Document> documents = new HashMap<>();
        final Map<UUID, UUID> coops = new HashMap<>();
        boolean unavailable;
        @Override protected List<UUID> profilesOf(UUID player) { return accounts.getOrDefault(player, List.of()); }
        @Override protected Document profileDocument(UUID profile) { return documents.get(profile); }
        @Override protected UUID coopOf(UUID profile) {
            if (unavailable) throw new IllegalStateException("Profile storage unavailable");
            return coops.get(profile);
        }
        @Override public boolean auctionEligible(UUID profile) { return true; }
    }
}
