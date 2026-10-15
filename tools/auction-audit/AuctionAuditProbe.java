import com.mongodb.client.*;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.auctions.*;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeType;
import dev.daweapon.commons.protocol.objects.auctions.*;
import dev.daweapon.service.auction.*;
import dev.daweapon.service.auction.endpoints.*;
import org.bson.Document;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionAuditProbe {
    static List<Document> rows = new ArrayList<>();
    static AtomicInteger archived = new AtomicInteger();
    static CountDownLatch writeGate;
    static CountDownLatch wrote;
    static CountDownLatch enteredWrite;

    @SuppressWarnings("unchecked")
    static MongoCollection<Document> collection(boolean inactive) {
        return (MongoCollection<Document>) Proxy.newProxyInstance(AuctionAuditProbe.class.getClassLoader(),
                new Class[]{MongoCollection.class}, (proxy, method, args) -> switch(method.getName()) {
            case "find" -> Proxy.newProxyInstance(AuctionAuditProbe.class.getClassLoader(), new Class[]{FindIterable.class},
                    (p, m, a) -> switch(m.getName()) {
                        case "first" -> rows.isEmpty() ? null : rows.getFirst();
                        case "into" -> { ((Collection<Document>)a[0]).addAll(rows); yield a[0]; }
                        default -> throw new UnsupportedOperationException(m.getName());
                    });
            case "insertOne" -> {
                if (inactive) archived.incrementAndGet();
                else if(writeGate != null) { enteredWrite.countDown(); writeGate.await(); wrote.countDown(); }
                yield null;
            }
            case "deleteOne" -> null;
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    static AuctionItem item(long end) {
        var type = new ItemAttributeType(); type.setValue("STONE");
        return new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(type), 1, Material.STONE),
                UUID.randomUUID(), end, true, 100L);
    }

    public static void main(String[] args) throws Exception {
        AuctionActiveDatabase.collection = collection(false);
        AuctionInactiveDatabase.collection = collection(true);
        var item = item(System.currentTimeMillis()+600_000);
        writeGate=new CountDownLatch(1); wrote=new CountDownLatch(1); enteredWrite=new CountDownLatch(1);
        var pending = CompletableFuture.supplyAsync(() -> new EndpointAddItem().handle(new AuctionAddItemProtocol.AuctionAddItemMessage(item,AuctionCategories.TOOLS),null));
        if (!enteredWrite.await(5, TimeUnit.SECONDS)) throw new AssertionError("Write never started");
        try {
            var storedType = item.getItem().getAttribute("item_type").getValue();
            if ("HYPERION".equals(storedType)) {
                System.out.println("REPRODUCED: submitted STONE attribute becomes HYPERION.");
            } else {
                if (!"STONE".equals(storedType)) throw new AssertionError("Unexpected item type: " + storedType);
                System.out.println("FIXED: submitted STONE attribute is preserved.");
            }
            if (pending.isDone()) System.out.println("REPRODUCED: listing response before database write completed.");
            else System.out.println("FIXED: listing response waits for database write.");
        } finally {writeGate.countDown();if(!wrote.await(5,TimeUnit.SECONDS))throw new AssertionError();writeGate=null;}
        if (!pending.get(5, TimeUnit.SECONDS).success()) throw new AssertionError();
        rows=List.of(item(System.currentTimeMillis()+600_000).toDocument());
        AuctionService.cacheService=new AuctionsCacheService();
        var browse = new EndpointFetchItems().handle(new AuctionFetchItemsProtocol.AuctionFetchItemsMessage(
                AuctionsSorting.LOWEST_BID,AuctionsFilter.BIN_ONLY,AuctionCategories.TOOLS),null);
        if (!browse.success() || browse.items().size()!=1) throw new AssertionError();
        System.out.println("FIXED: filtered browse sorts immutable cache results safely.");
        rows=List.of(item(System.currentTimeMillis()-10_000).toDocument());
        var cache=new AuctionsCacheService();
        if(!cache.getAuctions("TOOLS",AuctionsFilter.SHOW_ALL).isEmpty())
            throw new AssertionError("Expired listing should no longer appear in browse results");
        cache.getAuctions("TOOLS",AuctionsFilter.BIN_ONLY);
        if (archived.get()!=0) throw new AssertionError("Browse should never archive listings itself");
        System.out.println("FIXED: expired listings are filtered out of browse results; archival lifecycle (moving ended listings to AuctionInactiveDatabase) remains a separate, still-open concern.");
        rows=List.of();
        var missing=new EndpointFetchItem().handle(new AuctionFetchItemProtocol.AuctionFetchItemMessage(UUID.randomUUID()),null);
        if(missing.success() || missing.item()!=null)throw new AssertionError();
        new AuctionFetchItemProtocol().getReturnSerializer().serialize(missing);
        System.out.println("FIXED: missing ID returns a serializable not-found response.");
        var bigListing = new AuctionItem(item.getItem(),UUID.randomUUID(),1L,true,3_000_000_000L);
        if (!Long.valueOf(3_000_000_000L).equals(bigListing.getStartingPrice()))
            throw new AssertionError("Expected startingPrice to hold the full 3-billion-coin value");
        System.out.println("FIXED: 3-billion-coin listing no longer overflows starting-price (now a long).");
        System.out.println("No external database or live player state was accessed by this probe.");
    }
}
