package dev.daweapon.service.auction.endpoints;
import com.mongodb.client.*;
import net.minestom.server.item.Material;
import dev.daweapon.commons.protocol.objects.auctions.*;
import dev.daweapon.commons.skyblock.auctions.*;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.service.auction.*;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionMenuTest {
    @SuppressWarnings("unchecked")
    private MongoCollection<Document> collection(Document document) {
        return (MongoCollection<Document>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MongoCollection.class},
                (p,m,a) -> {
                    if (!m.getName().equals("find")) throw new UnsupportedOperationException(m.getName());
                    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{FindIterable.class},
                            (q,n,b) -> document);
                });
    }
    private Document listing(long price) {
        return new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis()+60000, true, price).toDocument();
    }
    @Test void missingListingHasSerializableFailure() {
        var active=AuctionActiveDatabase.collection; var inactive=AuctionInactiveDatabase.collection;
        try {
            AuctionActiveDatabase.collection=collection(null); AuctionInactiveDatabase.collection=collection(null);
            var result=new EndpointFetchItem().handle(new AuctionFetchItemProtocol.AuctionFetchItemMessage(UUID.randomUUID()), null);
            var serializer=new AuctionFetchItemProtocol().getReturnSerializer();
            var decoded=serializer.deserialize(serializer.serialize(result));
            assertFalse(decoded.success()); assertNull(decoded.item()); assertNotNull(decoded.error());
        } finally { AuctionActiveDatabase.collection=active; AuctionInactiveDatabase.collection=inactive; }
    }
    @Test void activeWinsAndInactiveIsFallback() {
        var active=AuctionActiveDatabase.collection; var inactive=AuctionInactiveDatabase.collection;
        try {
            var current=listing(100);var old=listing(50);
            AuctionActiveDatabase.collection=collection(current); AuctionInactiveDatabase.collection=collection(old);
            var request=new AuctionFetchItemProtocol.AuctionFetchItemMessage(UUID.fromString(current.getString("_id")));
            assertEquals(100,new EndpointFetchItem().handle(request,null).item().getStartingPrice());
            AuctionActiveDatabase.collection=collection(null);
            assertEquals(50,new EndpointFetchItem().handle(request,null).item().getStartingPrice());
        } finally { AuctionActiveDatabase.collection=active; AuctionInactiveDatabase.collection=inactive; }
    }
    @Test void oldIntegerAndNewLongPricesBothLoad() {
        var old=listing(100);old.put("starting-price",100);
        assertEquals(100L,AuctionItem.fromDocument(old).getStartingPrice());
        var large=listing(3_000_000_000L);
        assertEquals(3_000_000_000L,AuctionItem.fromDocument(large).getStartingPrice());
        var serializer=new dev.daweapon.commons.protocol.serializers.AuctionItemSerializer<AuctionItem>();
        assertEquals(3_000_000_000L,serializer.deserialize(serializer.serialize(AuctionItem.fromDocument(large))).getStartingPrice());
    }
    @Test void malformedBidCannotBreakOtherListingsAndSortUsesCurrentBids() {
        var previous=AuctionService.cacheService;var low=listing(100);var high=listing(200);var invalid=listing(300);
        low.put("bids",List.of(new AuctionItem.Bid(1L,UUID.randomUUID(),500L).toString()));invalid.put("bids",List.of("broken"));
        try {
            AuctionService.cacheService=new AuctionsCacheService() {
                @Override public List<Document> getAuctions(String category,AuctionsFilter filter) {return List.of(invalid,high,low);}
            };
            for(var sort:List.of(AuctionsSorting.HIGHEST_BID,AuctionsSorting.MOST_BIDS)) {
                var result=new EndpointFetchItems().handle(new AuctionFetchItemsProtocol.AuctionFetchItemsMessage(sort,AuctionsFilter.SHOW_ALL,AuctionCategories.TOOLS),null);
                assertEquals(2,result.items().size());assertEquals(low.getString("_id"),result.items().getFirst().getUuid().toString());
            }
        } finally {AuctionService.cacheService=previous;}
    }
    @Test void cachedListingsDisappearWhenTheirEndTimePasses() {
        var document=listing(100);
        document.put("end", System.currentTimeMillis() + 60_000);
        var cache=new AuctionsCacheService() {@Override public List<Document> getDocuments(String category) {return List.of(document);}};
        assertEquals(1,cache.getAuctions("TOOLS",AuctionsFilter.SHOW_ALL).size());
        document.put("end",System.currentTimeMillis()-1);
        assertTrue(cache.getAuctions("TOOLS",AuctionsFilter.SHOW_ALL).isEmpty());
    }
    @Test void sortingWorksWithImmutableFilteredResultsWithoutChangingCache() {
        var previous=AuctionService.cacheService;
        var documents=List.of(listing(100),listing(200));
        try {
            AuctionService.cacheService=new AuctionsCacheService() {
                @Override public List<Document> getAuctions(String category, AuctionsFilter filter) { return documents; }
            };
            for(var filter:AuctionsFilter.values()) {
                var result=new EndpointFetchItems().handle(new AuctionFetchItemsProtocol.AuctionFetchItemsMessage(
                        AuctionsSorting.HIGHEST_BID,filter,AuctionCategories.TOOLS),null);
                assertTrue(result.success()); assertEquals(200,result.items().getFirst().getStartingPrice());
            }
            assertEquals(100L,documents.getFirst().getLong("starting-price"));
        } finally { AuctionService.cacheService=previous; }
    }
}
