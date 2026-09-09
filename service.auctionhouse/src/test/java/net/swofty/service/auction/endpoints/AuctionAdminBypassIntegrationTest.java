package net.swofty.service.auction.endpoints;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionAdminBypassProtocol;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.auctions.AuctionRules;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.service.auction.AuctionActiveDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionAdminBypassIntegrationTest {
    private MongoClient client;
    private String database;
    private MongoCollection<Document> original;
    private final EndpointAdminBypass endpoint = new EndpointAdminBypass();

    @BeforeEach
    void setup() {
        String uri = System.getenv("AUCTION_TEST_MONGO_URI");
        Assumptions.assumeTrue(uri != null);
        client = MongoClients.create(uri);
        database = "auction_test_" + UUID.randomUUID().toString().replace("-", "");
        original = AuctionActiveDatabase.collection;
        AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("active");
    }

    @AfterEach
    void cleanup() {
        if (client == null) return;
        client.getDatabase(database).drop();
        client.close();
        AuctionActiveDatabase.collection = original;
    }

    @Test
    void skipsBinGraceAndAuctionEndIndependently() {
        AuctionItem item = listing(true);
        assertTrue(handle(item, AuctionAdminBypassProtocol.Timer.BIN_GRACE).success());
        Document afterGrace = stored(item);
        assertTrue(afterGrace.getLong("created_at") <= System.currentTimeMillis() - AuctionRules.BIN_PURCHASE_GRACE);

        assertTrue(handle(item, AuctionAdminBypassProtocol.Timer.AUCTION_END).success());
        assertTrue(stored(item).getLong("end") <= System.currentTimeMillis());
        assertFalse(handle(item, AuctionAdminBypassProtocol.Timer.AUCTION_END).success());
    }

    @Test
    void rejectsBinGraceForARegularAuctionAndUnknownListings() {
        AuctionItem item = listing(false);
        assertFalse(handle(item, AuctionAdminBypassProtocol.Timer.BIN_GRACE).success());
        assertFalse(endpoint.handle(new AuctionAdminBypassProtocol.Request(UUID.randomUUID(),
                AuctionAdminBypassProtocol.Timer.AUCTION_END), null).success());
    }

    private AuctionItem listing(boolean bin) {
        AuctionItem item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 60_000, bin, 100L);
        AuctionActiveDatabase.collection.insertOne(item.toDocument().append("created_at", System.currentTimeMillis()));
        return item;
    }

    private AuctionAdminBypassProtocol.Response handle(AuctionItem item, AuctionAdminBypassProtocol.Timer timer) {
        return endpoint.handle(new AuctionAdminBypassProtocol.Request(item.getUuid(), timer), null);
    }

    private Document stored(AuctionItem item) {
        return AuctionActiveDatabase.collection.find(new Document("_id", item.getUuid().toString())).first();
    }
}
