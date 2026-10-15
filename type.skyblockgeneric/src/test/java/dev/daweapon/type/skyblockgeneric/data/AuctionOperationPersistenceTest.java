package dev.daweapon.type.skyblockgeneric.data;

import dev.daweapon.DataAPI;
import dev.daweapon.transaction.Transaction;
import dev.daweapon.transaction.TransactionConsumer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAuctionOperations;
import org.junit.jupiter.api.*;
import java.lang.reflect.Proxy;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuctionOperationPersistenceTest {
    @BeforeAll static void attributes() { ItemAttribute.registerItemAttributes(); }

    @Test void staleSnapshotCannotErasePendingOrSettledOperation() {
        for (var saved : new DatapointAuctionOperations.State[]{new DatapointAuctionOperations.State(1, "pending-request"),
                new DatapointAuctionOperations.State(2, null)}) {
            var handler = SkyBlockDataHandler.initUserWithDefaultData(UUID.randomUUID(), UUID.randomUUID());
            var document = handler.toProfileDocument();
            Transaction transaction = (Transaction) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Transaction.class}, (p, m, a) -> {
                if (m.getName().equals("get")) return a[0].equals(SkyBlockDataHandler.Data.AUCTION_OPERATIONS.profileField())
                        ? new DatapointAuctionOperations("auction_operations", saved).getSerializedValue() : null;
                fail("A stale snapshot must not write coins, inventory, or operations"); return null;
            });
            DataAPI api = (DataAPI) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DataAPI.class}, (p, m, a) -> {
                ((TransactionConsumer) a[1]).accept(transaction); return null;
            });
            assertThrows(IllegalStateException.class, () -> SkyBlockDataHandler.persistProfileDocument(api, handler.getCurrentProfileId(), document));
        }
    }

    @Test void missingReservationFieldCannotPersistCoinsWithoutRecoveryRecord() {
        var handler = SkyBlockDataHandler.initUserWithDefaultData(UUID.randomUUID(), UUID.randomUUID());
        var document = handler.toProfileDocument();
        document.remove(SkyBlockDataHandler.Data.AUCTION_OPERATIONS.getKey());
        assertThrows(IllegalStateException.class, () -> SkyBlockDataHandler.persistProfileDocument(null, handler.getCurrentProfileId(), document));
    }
}
