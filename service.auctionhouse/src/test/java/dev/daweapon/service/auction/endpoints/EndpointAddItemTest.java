package dev.daweapon.service.auction.endpoints;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import net.minestom.server.item.Material;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeType;
import dev.daweapon.service.auction.AuctionActiveDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EndpointAddItemTest {
    @Test
    void preservesItemMetadataOnInsert() throws Exception {
        var type = new ItemAttributeType();
        type.setValue("STONE");
        var item = new UnderstandableSkyBlockItem(null, List.of(type), 17, Material.STONE);
        verify(item, false);
        assertEquals("STONE", type.getValue());
    }

    @Test
    void supportsItemsWithoutLegacyTypeAttribute() throws Exception {
        verify(new UnderstandableSkyBlockItem(null, List.of(), 3, Material.DIAMOND), false);
    }

    @SuppressWarnings("unchecked")
    private void verify(UnderstandableSkyBlockItem item, boolean existing) throws Exception {
        var saved = new CompletableFuture<Document>();
        var previous = AuctionActiveDatabase.collection;
        AuctionActiveDatabase.collection = (MongoCollection<Document>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{MongoCollection.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "find" -> Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{FindIterable.class},
                            (p, m, a) -> {
                                if (!m.getName().equals("first")) throw new UnsupportedOperationException(m.getName());
                                return existing ? new Document() : null;
                            });
                    case "insertOne" -> { assertFalse(existing); saved.complete((Document) args[0]); yield null; }
                    case "replaceOne" -> { assertTrue(existing); saved.complete((Document) args[1]); yield null; }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        try {
            var auction = new AuctionItem(item, UUID.randomUUID(), System.currentTimeMillis() + 60_000, true, 100L);
            String original = item.serialize();
            var response = new EndpointAddItem(new AuctionTestProfiles()).handle(
                    new AuctionAddItemProtocol.AuctionAddItemMessage(auction, AuctionCategories.TOOLS), null);
            var document = saved.get(5, TimeUnit.SECONDS);
            assertTrue(response.success());
            assertEquals(auction.getUuid(), response.uuid());
            assertEquals(original, document.getString("item"));
            assertEquals(original, item.serialize());
        } finally {
            AuctionActiveDatabase.collection = previous;
        }
    }
}
