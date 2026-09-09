package net.swofty.service.auction.endpoints;

import com.mongodb.client.*;
import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol.*;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.service.auction.*;
import org.bson.Document;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionClaimsIntegrationTest {
    MongoClient client; String database;
    MongoCollection<Document> active,inactive;
    final EndpointClaim endpoint=new EndpointClaim(new AuctionTestProfiles());
    @BeforeEach void setup() {
        String uri=System.getenv("AUCTION_TEST_MONGO_URI");Assumptions.assumeTrue(uri!=null);
        client=MongoClients.create(uri);database="auction_test_"+UUID.randomUUID().toString().replace("-","");
        active=AuctionActiveDatabase.collection;inactive=AuctionInactiveDatabase.collection;
        AuctionActiveDatabase.collection=client.getDatabase(database).getCollection("active");
        AuctionInactiveDatabase.collection=client.getDatabase(database).getCollection("inactive");
    }
    @AfterEach void cleanup() {
        if(client==null)return;client.getDatabase(database).drop();client.close();
        AuctionActiveDatabase.collection=active;AuctionInactiveDatabase.collection=inactive;
    }
    AuctionItem listing(boolean bin,List<AuctionItem.Bid> bids) {
        var item=new AuctionItem(new UnderstandableSkyBlockItem(null,List.of(),1,Material.STONE),UUID.randomUUID(),System.currentTimeMillis()-1000,bin,100L);
        item.setBids(bids);AuctionActiveDatabase.collection.insertOne(item.toDocument());return item;
    }
    Response claim(AuctionItem item,UUID actor,UUID profile) {return endpoint.handle(new Request(item.getUuid(),actor,profile),null);}
    @Test void sellerWinnerAndLoserReceiveCorrectIndependentEntitlements() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        var item=listing(false,List.of(new AuctionItem.Bid(1L,a,100L),new AuctionItem.Bid(2L,b,150L),new AuctionItem.Bid(3L,a,200L)));
        var seller=claim(item,item.getOriginator(),AuctionTestProfiles.profile(item.getOriginator())).receipt();assertEquals(200,seller.coins());assertNull(seller.item());
        assertTrue(seller.sold());assertEquals(200,seller.salePrice());assertEquals(item.getItem().serialize(),seller.auctionItem());
        var winner=claim(item,a,AuctionTestProfiles.profile(a)).receipt();assertEquals(100,winner.coins());assertEquals(item.getItem().serialize(),winner.item());
        assertTrue(winner.sold());assertEquals(200,winner.salePrice());assertEquals(item.getItem().serialize(),winner.auctionItem());
        var loser=claim(item,b,AuctionTestProfiles.profile(b)).receipt();assertEquals(150,loser.coins());assertNull(loser.item());
    }
    @Test void binAndUnsoldItemClaimsAreSupported() {
        UUID buyer=UUID.randomUUID();var sold=listing(true,List.of(new AuctionItem.Bid(1L,buyer,100L)));
        assertNotNull(claim(sold,buyer,AuctionTestProfiles.profile(buyer)).receipt().item());
        assertEquals(100,claim(sold,sold.getOriginator(),AuctionTestProfiles.profile(sold.getOriginator())).receipt().coins());
        for(boolean bin:List.of(true,false)) {
            var unsold=listing(bin,List.of());var receipt=claim(unsold,unsold.getOriginator(),AuctionTestProfiles.profile(unsold.getOriginator())).receipt();
            assertNotNull(receipt.item());assertEquals(0,receipt.coins());
        }
    }
    @Test void retriesSurviveNewEndpointAndWireRoundTripButRejectAnotherProfile() {
        var item=listing(false,List.of());UUID profile=AuctionTestProfiles.profile(item.getOriginator());var request=new Request(item.getUuid(),item.getOriginator(),profile);
        var original=endpoint.handle(request,null);var retry=new EndpointClaim(new AuctionTestProfiles()).handle(request,null);
        var serializer=new AuctionClaimProtocol().getReturnSerializer();
        assertEquals(original,serializer.deserialize(serializer.serialize(retry)));
        assertFalse(claim(item,item.getOriginator(),UUID.randomUUID()).success());
    }
    @Test void concurrentProfilesCannotCaptureSameEntitlement() throws Exception {
        var item=listing(false,List.of());var start=new CountDownLatch(1);var responses=new ArrayList<Future<Response>>();
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()) {
            for(int i=0;i<24;i++) { UUID profile = i == 0 ? AuctionTestProfiles.profile(item.getOriginator()) : UUID.randomUUID(); responses.add(executor.submit(()->{start.await();return claim(item,item.getOriginator(),profile);})); }
            start.countDown();int winners=0;
            for(var response:responses)if(response.get(10,TimeUnit.SECONDS).success())winners++;
            assertEquals(1,winners);
        }
        var stored=AuctionActiveDatabase.collection.find(new Document("_id",item.getUuid().toString())).first();
        assertEquals(1,stored.get("claims",Document.class).size());
    }
    @Test void concurrentSameProfileRetriesReturnIdenticalReceipts() throws Exception {
        var item=listing(false,List.of());UUID profile=AuctionTestProfiles.profile(item.getOriginator());var start=new CountDownLatch(1);var responses=new ArrayList<Future<Response>>();
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()) {
            for(int i=0;i<24;i++)responses.add(executor.submit(()->{start.await();return claim(item,item.getOriginator(),profile);}));
            start.countDown();Response first=responses.getFirst().get(10,TimeUnit.SECONDS);
            assertTrue(first.success());for(var response:responses)assertEquals(first,response.get(10,TimeUnit.SECONDS));
        }
    }
    @Test void rejectsEarlyMissingAndUnrelatedClaimsAndSupportsArchivedLegacyListing() {
        var item=listing(false,List.of());
        assertFalse(claim(item,UUID.randomUUID(),UUID.randomUUID()).success());
        AuctionActiveDatabase.collection.updateOne(new Document("_id",item.getUuid().toString()),new Document("$set",new Document("end",System.currentTimeMillis()+60000)));
        assertFalse(claim(item,item.getOriginator(),UUID.randomUUID()).success());
        AuctionActiveDatabase.collection.deleteMany(new Document());
        assertFalse(claim(item,item.getOriginator(),UUID.randomUUID()).success());
        AuctionInactiveDatabase.collection.insertOne(item.toDocument());assertTrue(claim(item,item.getOriginator(),AuctionTestProfiles.profile(item.getOriginator())).success());
    }
}
