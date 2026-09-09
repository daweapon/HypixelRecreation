package net.swofty.service.auction;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionArchiveIntegrationTest {
    private MongoClient client;
    private MongoCollection<Document> previousActive;
    private MongoCollection<Document> previousInactive;
    private String database;

    @BeforeEach void setUp() {
        String uri = System.getenv("AUCTION_TEST_MONGO_URI");
        Assumptions.assumeTrue(uri != null, "Requires isolated MongoDB via AUCTION_TEST_MONGO_URI");
        client = MongoClients.create(uri);
        database = "auction_archive_" + UUID.randomUUID().toString().replace("-", "");
        previousActive = AuctionActiveDatabase.collection;
        previousInactive = AuctionInactiveDatabase.collection;
        AuctionActiveDatabase.collection = client.getDatabase(database).getCollection("active");
        AuctionInactiveDatabase.collection = client.getDatabase(database).getCollection("inactive");
    }

    @AfterEach void cleanUp() {
        if (client == null) return;
        client.getDatabase(database).drop();
        AuctionActiveDatabase.collection = previousActive;
        AuctionInactiveDatabase.collection = previousInactive;
        client.close();
    }

    @Test void movesOnlyEndedListingsAndIsIdempotent() {
        Document ended = listing(System.currentTimeMillis() - 1);
        Document active = listing(System.currentTimeMillis() + 60_000);
        var archive = new AuctionArchiveService();

        assertEquals(1, archive.archiveExpired(System.currentTimeMillis()));
        assertNull(find(AuctionActiveDatabase.collection, ended));
        assertNotNull(find(AuctionInactiveDatabase.collection, ended));
        assertNotNull(find(AuctionActiveDatabase.collection, active));
        assertEquals(0, archive.archiveExpired(System.currentTimeMillis()));
        assertEquals(1, AuctionInactiveDatabase.collection.countDocuments());
    }

    @Test void staleArchiverCannotEraseClaimsCopiedByANewerAttempt() {
        Document original = listing(System.currentTimeMillis() - 1);
        Document stale = new Document(original);
        String player = UUID.randomUUID().toString();
        String receipt = "saved-claim-receipt";
        AuctionActiveDatabase.collection.updateOne(id(original), new Document("$set",
                new Document("claims." + player, receipt).append("bid_profiles." + player, UUID.randomUUID().toString())));
        Document current = find(AuctionActiveDatabase.collection, original);
        var archive = new AuctionArchiveService();

        assertTrue(archive.archive(current));
        assertFalse(archive.archive(stale));

        Document archived = find(AuctionInactiveDatabase.collection, original);
        assertEquals(receipt, archived.get("claims", Document.class).getString(player));
        assertNotNull(archived.get("bid_profiles", Document.class).getString(player));
        assertEquals(1, AuctionInactiveDatabase.collection.countDocuments());
    }

    @Test void changedActiveDocumentIsRetriedBeforeDeletion() {
        Document original = listing(System.currentTimeMillis() - 1);
        Document stale = new Document(original);
        AuctionActiveDatabase.collection.updateOne(id(original), new Document("$set", new Document("seller_profile", UUID.randomUUID().toString())));
        var archive = new AuctionArchiveService();

        assertFalse(archive.archive(stale));
        assertNotNull(find(AuctionActiveDatabase.collection, original));
        assertEquals(1, archive.archiveExpired(System.currentTimeMillis()));
        assertNull(find(AuctionActiveDatabase.collection, original));
        assertNotNull(find(AuctionInactiveDatabase.collection, original).getString("seller_profile"));
    }

    private static Document listing(long end) {
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), end, false, 100L);
        Document document = item.toDocument().append("category", "TOOLS");
        AuctionActiveDatabase.collection.insertOne(document);
        return document;
    }

    private static Document find(MongoCollection<Document> collection, Document listing) {
        return collection.find(id(listing)).first();
    }

    private static Document id(Document listing) {
        return new Document("_id", listing.getString("_id"));
    }
}
