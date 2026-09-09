package net.swofty.type.skyblockgeneric.auction;

import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol.*;
import net.swofty.commons.skyblock.auctions.*;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionCreationTest {
    @BeforeAll static void attributes() { ItemAttribute.registerItemAttributes(); }
    private final DatapointDouble purse = new DatapointDouble("coins", 1000D);
    private final DatapointAuctionEscrow data = new DatapointAuctionEscrow("escrow");
    private final List<UUID> owned = new ArrayList<>();

    private AuctionAddItemMessage stage() {
        var item = new SkyBlockItem(Material.STONE);
        data.getValue().setItem(item);
        var message = new AuctionAddItemMessage(new AuctionItem(item.toUnderstandable(), UUID.randomUUID(),
                System.currentTimeMillis() + 600000, true, 100L), AuctionCategories.TOOLS);
        assertTrue(AuctionCreation.stage(data.getValue(), purse, message, 100));
        return message;
    }

    @Test void retryAfterReloadDoesNotChargeAgain() {
        var message = stage();
        String pending = data.getValue().getPendingCreation();
        var restored = data.getSerializer().deserialize(data.getSerializedValue());
        assertNull(restored.getItem());
        assertEquals(100, restored.getPendingFee());
        assertTrue(AuctionCreation.stage(restored, purse, message, 900));
        assertEquals(pending, restored.getPendingCreation());
        assertEquals(900D, purse.getValue());
        assertTrue(owned.isEmpty());
        assertEquals(pending, data.getSerializer().clone(restored).getPendingCreation());
    }

    @Test void confirmedCreationRecordsOwnershipExactlyOnce() {
        var message = stage();
        var result = new AuctionAddItemResponse(message.item().getUuid(), true, null);
        AuctionCreation.settle(data.getValue(), purse, owned, result);
        AuctionCreation.settle(data.getValue(), purse, owned, result);
        assertEquals(List.of(message.item().getUuid()), owned);
        assertEquals(900D, purse.getValue());
        assertFalse(data.getValue().isPending());
        assertNull(data.getValue().getItem());
    }

    @Test void rejectionRestoresItemAndFeeOncePreservingOtherCredits() {
        var message = stage();
        purse.setValue(purse.getValue() + 75);
        var result = new AuctionAddItemResponse(message.item().getUuid(), false, "rejected");
        AuctionCreation.settle(data.getValue(), purse, owned, result);
        AuctionCreation.settle(data.getValue(), purse, owned, result);
        assertEquals(1075D, purse.getValue());
        assertTrue(owned.isEmpty());
        assertFalse(data.getValue().isPending());
        String item = data.getValue().getItem().toUnderstandable().serialize();
        assertEquals(message.item().getItem().serialize(), item);
        assertEquals(item, data.getSerializer().deserialize(data.getSerializedValue()).getItem().toUnderstandable().serialize());
    }

    @Test void unrelatedResponseCannotReleaseEscrow() {
        stage();
        assertThrows(IllegalStateException.class, () -> AuctionCreation.settle(data.getValue(), purse, owned,
                new AuctionAddItemResponse(UUID.randomUUID(), false, "rejected")));
        assertTrue(data.getValue().isPending());
        assertEquals(900D, purse.getValue());
    }

    @Test void insufficientFundsLeaveItemAndBalanceUntouched() {
        var message = stage();
        var escrow = new DatapointAuctionEscrow.AuctionEscrow(new SkyBlockItem(Material.STONE), 100, 600000, true);
        var original = escrow.getItem();
        assertFalse(AuctionCreation.stage(escrow, purse, message, 901));
        assertSame(original, escrow.getItem());
        assertFalse(escrow.isPending());
        assertEquals(900D, purse.getValue());
    }

    @Test void pendingCreationRetainsOriginalSellerProfileAfterReload() {
        var item = new SkyBlockItem(Material.STONE);
        var escrow = data.getValue(); escrow.setItem(item);
        var auction = new AuctionItem(item.toUnderstandable(), UUID.randomUUID(), System.currentTimeMillis() + 600000, true, 100L);
        UUID profile = UUID.randomUUID(); auction.setSellerProfile(profile);
        var request = new AuctionAddItemMessage(auction, AuctionCategories.TOOLS);
        assertTrue(AuctionCreation.stage(escrow, purse, request, 100));
        var reloaded = data.getSerializer().deserialize(data.getSerializedValue());
        var saved = new net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol().getSerializer().deserialize(reloaded.getPendingCreation());
        assertEquals(profile, saved.item().getSellerProfile());
        assertEquals(auction.getUuid(), saved.item().getUuid());
        assertEquals(auction.getOriginator(), saved.item().getOriginator());
        assertTrue(AuctionCreation.stage(reloaded, purse, saved, 100));
        assertEquals(900D, purse.getValue());
        AuctionCreation.settle(reloaded, purse, owned, new AuctionAddItemResponse(auction.getUuid(), true, null));
        assertEquals(List.of(auction.getUuid()), owned);
        assertFalse(reloaded.isPending());
    }

    @Test void legacyEscrowRemainsReadable() {
        var restored = data.getSerializer().deserialize("{\"item\":\"\",\"price\":50,\"duration\":21600000,\"bin\":false}");
        assertFalse(restored.isPending());
        assertEquals(0, restored.getPendingFee());
        assertEquals(50, restored.getPrice());
    }
}
