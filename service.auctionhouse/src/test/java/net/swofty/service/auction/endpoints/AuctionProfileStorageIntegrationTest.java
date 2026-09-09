package net.swofty.service.auction.endpoints;

import com.mongodb.client.*;
import net.minestom.server.item.Material;
import net.swofty.commons.data.ProfileIndexes;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.protocol.objects.auctions.*;
import net.swofty.commons.skyblock.CoopStorage;
import net.swofty.commons.skyblock.SkyBlockProfileFields;
import net.swofty.commons.skyblock.auctions.*;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.service.auction.*;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionProfileStorageIntegrationTest {
    @Test void realProfileStorageResolvesLegacyOwnerAndRestrictsOnlyParticipatingCoopProfiles() {
        String mongo = System.getenv("AUCTION_TEST_MONGO_URI"), redis = System.getenv("AUCTION_TEST_REDIS_URI");
        Assumptions.assumeTrue(mongo != null && redis != null, "Requires disposable Mongo and Redis instances");
        SwoftyData.bootstrap(redis);
        var oldActive = AuctionActiveDatabase.collection; var oldInactive = AuctionInactiveDatabase.collection;
        String database = "auction_test_" + UUID.randomUUID().toString().replace("-", "");
        try (var client = MongoClients.create(mongo)) {
            AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("active");
            AuctionInactiveDatabase.collection = client.getDatabase(database).getCollection("inactive");
            try {
                UUID seller = UUID.randomUUID(), sellerProfile = UUID.randomUUID(), sellerOther = UUID.randomUUID();
                UUID buyer = UUID.randomUUID(), buyerProfile = UUID.randomUUID(), buyerOther = UUID.randomUUID();
                SwoftyData.account().set(seller, ProfileIndexes.SKYBLOCK, sellerOther + ";" + sellerProfile + "," + sellerOther);
                SwoftyData.account().set(buyer, ProfileIndexes.SKYBLOCK, buyerOther + ";" + buyerProfile + "," + buyerOther);
                var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE), seller,
                        System.currentTimeMillis() + 600000, true, 100L);
                var creation = new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS);
                assertTrue(new EndpointAddItem().handle(creation, null).success());
                AuctionActiveDatabase.collection.updateOne(new Document("_id", item.getUuid().toString()),
                        new Document("$set", new Document("created_at", System.currentTimeMillis() - 21_000)));
                SwoftyData.profile().set(sellerProfile, SkyBlockProfileFields.DOCUMENT,
                        new Document("auction_owned", item.getUuid().toString()).toJson());
                SwoftyData.profile().set(buyerProfile, SkyBlockProfileFields.DOCUMENT, new Document().toJson());
                SwoftyData.profile().set(buyerOther, SkyBlockProfileFields.DOCUMENT, new Document().toJson());
                CoopStorage.write(new Document("_id", UUID.randomUUID().toString()).append("originator", seller.toString())
                        .append("members", List.of(seller.toString(), buyer.toString())).append("memberInvites", List.of())
                        .append("memberProfiles", List.of(sellerProfile.toString(), buyerProfile.toString())));
                var lookup = new AuctionProfiles();
                assertEquals(sellerProfile, lookup.sellerProfile(item));
                assertTrue(lookup.sameCoop(sellerProfile, buyerProfile));
                assertFalse(lookup.sameCoop(sellerProfile, buyerOther));
                var expected = AuctionAddItemProtocol.ExpectedState.of(item);
                item.setBids(new ArrayList<>(List.of(new AuctionItem.Bid(System.currentTimeMillis(), buyer, 100L))));
                var bid = new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS, expected);
                assertFalse(new EndpointBid().handle(new AuctionBidProtocol.Request(UUID.randomUUID(), buyer, buyerProfile, bid), null).success());
                assertTrue(new EndpointBid().handle(new AuctionBidProtocol.Request(UUID.randomUUID(), buyer, buyerOther, bid), null).success());
                var claims = new EndpointClaim();
                assertFalse(claims.handle(new AuctionClaimProtocol.Request(item.getUuid(), seller, sellerOther), null).success());
                assertEquals(100, claims.handle(new AuctionClaimProtocol.Request(item.getUuid(), seller, sellerProfile), null).receipt().coins());
                assertNotNull(claims.handle(new AuctionClaimProtocol.Request(item.getUuid(), buyer, buyerOther), null).receipt().item());
                var modern = new AuctionItem(item.getItem(), seller, System.currentTimeMillis() + 600000, true, 100L);
                modern.setSellerProfile(sellerProfile);
                assertTrue(new EndpointAddItem().handle(new AuctionAddItemProtocol.AuctionAddItemMessage(modern, AuctionCategories.TOOLS), null).success());
            } finally { client.getDatabase(database).drop(); }
        } finally {
            AuctionActiveDatabase.collection = oldActive; AuctionInactiveDatabase.collection = oldInactive;
            SwoftyData.shutdown();
        }
    }
}
