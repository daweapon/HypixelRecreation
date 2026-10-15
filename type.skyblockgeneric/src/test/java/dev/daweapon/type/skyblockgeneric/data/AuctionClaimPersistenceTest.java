package dev.daweapon.type.skyblockgeneric.data;

import dev.daweapon.DataAPI;
import dev.daweapon.transaction.Transaction;
import dev.daweapon.transaction.TransactionConsumer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionClaimPersistenceTest {
    @BeforeAll static void attributes() { ItemAttribute.registerItemAttributes(); }
    @Test void profileFieldsAndSnapshotAreWrittenWithinOneTransaction() {
        var handler=SkyBlockDataHandler.initUserWithDefaultData(UUID.randomUUID(),UUID.randomUUID());
        Document document=handler.toProfileDocument();
        var writes=new HashMap<Object,Object>();int[] transactions={0};
        Transaction transaction=(Transaction)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Transaction.class},(p,m,a)->{
            if (m.getName().equals("get")) return null;
            assertEquals("set",m.getName());writes.put(a[0],a[1]);return null;
        });
        DataAPI api=(DataAPI)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{DataAPI.class},(p,m,a)->{
            assertEquals("transaction",m.getName());transactions[0]++;((TransactionConsumer)a[1]).accept(transaction);return null;
        });
        SkyBlockDataHandler.persistProfileDocument(api,handler.getCurrentProfileId(),document);
        assertEquals(1,transactions[0]);
        for(var field:List.of(SkyBlockDataHandler.Data.COINS,SkyBlockDataHandler.Data.INVENTORY,
                SkyBlockDataHandler.Data.AUCTION_DELIVERED_CLAIMS,SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS,
                SkyBlockDataHandler.Data.AUCTION_INACTIVE_BIDS, SkyBlockDataHandler.Data.AUCTION_OPERATIONS)) assertEquals(document.getString(field.getKey()),writes.get(field.profileField()));
        assertEquals(document.toJson(),writes.get(ProfilesDatabase.DOCUMENT));
    }
    @Test void staleProfileSnapshotCannotEraseCommittedReceipts() {
        var handler=SkyBlockDataHandler.initUserWithDefaultData(UUID.randomUUID(),UUID.randomUUID());
        var document=handler.toProfileDocument();
        Transaction transaction=(Transaction)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Transaction.class},(p,m,a)->{
            if (m.getName().equals("get")) return UUID.randomUUID().toString();
            fail("Stale claim state must not write");return null;
        });
        DataAPI api=(DataAPI)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{DataAPI.class},(p,m,a)->{
            ((TransactionConsumer)a[1]).accept(transaction);return null;
        });
        assertThrows(IllegalStateException.class,()->SkyBlockDataHandler.persistProfileDocument(api,handler.getCurrentProfileId(),document));
    }
    @Test void missingCriticalFieldCannotWriteAPartialClaim() {
        var handler=SkyBlockDataHandler.initUserWithDefaultData(UUID.randomUUID(),UUID.randomUUID());
        var document=handler.toProfileDocument();document.remove(SkyBlockDataHandler.Data.INVENTORY.getKey());
        assertThrows(IllegalStateException.class,()->SkyBlockDataHandler.persistProfileDocument(null,handler.getCurrentProfileId(),document));
    }
}
