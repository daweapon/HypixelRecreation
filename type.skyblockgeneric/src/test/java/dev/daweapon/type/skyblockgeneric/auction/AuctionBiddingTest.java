package dev.daweapon.type.skyblockgeneric.auction;

import net.minestom.server.item.Material;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionBidProtocol;
import dev.daweapon.commons.protocol.objects.auctions.AuctionBidProtocol.*;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAuctionOperations;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionBiddingTest {
    @Test void disconnectedProfileRetainsExactRequestAndChargedAmountAcrossReload() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        AuctionBidding.stage(pending, purse, request);
        assertEquals(880, purse.getValue());
        var reloaded = reload(pending);
        var saved = new AuctionBidProtocol().getSerializer().deserialize(reloaded.getValue().pending());
        assertEquals(request.operation(), saved.operation());
        assertEquals(request.profile(), saved.profile());
        assertEquals(request.player(), saved.player());
        assertEquals(request.bid().expected(), saved.bid().expected());
        assertEquals(request.bid().item().getBids(), saved.bid().item().getBids());
        assertThrows(IllegalStateException.class, () -> AuctionBidding.stage(reloaded, purse, saved));
        assertEquals(880, purse.getValue());
        var active = new ArrayList<UUID>();
        AuctionBidding.settle(reloaded, purse, active, List.of(), saved, response(saved, true));
        assertEquals(List.of(request.bid().item().getUuid()), active);
        assertEquals(880, purse.getValue());
        assertNull(reloaded.getValue().pending());
    }

    @Test void rejectionRefundsOnceAndReplayAfterReloadCannotRefundAgain() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        AuctionBidding.stage(pending, purse, request);
        var active = new ArrayList<UUID>();
        AuctionBidding.settle(pending, purse, active, List.of(), request, response(request, false));
        assertEquals(1000, purse.getValue());
        assertTrue(active.isEmpty());
        var reloaded = reload(pending);
        assertThrows(IllegalStateException.class, () -> AuctionBidding.settle(reloaded, purse, active, List.of(), request, response(request, false)));
        assertEquals(1000, purse.getValue());
        assertEquals(2, reloaded.getValue().revision());
    }

    @Test void failedLocalSettlementCanReplayThePersistedReservation() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        AuctionBidding.stage(pending, purse, request);
        var durable = reload(pending);
        AuctionBidding.settle(pending, purse, new ArrayList<>(), List.of(), request, response(request, false));
        // Simulate a process stopping before its atomic settlement snapshot commits.
        var reloadedPurse = new DatapointDouble("coins", 880D);
        AuctionBidding.settle(durable, reloadedPurse, new ArrayList<>(), List.of(), request, response(request, false));
        assertEquals(1000, reloadedPurse.getValue());
        assertNull(durable.getValue().pending());
    }

    @Test void anotherProfilesResponseCannotClearOrRefundReservation() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        AuctionBidding.stage(pending, purse, request);
        var before = pending.getValue();
        assertThrows(IllegalStateException.class, () -> AuctionBidding.settle(pending, purse, new ArrayList<>(), List.of(), request,
                new Response(request.operation(), request.bid().item().getUuid(), UUID.randomUUID(), false, "rejected")));
        assertEquals(before, pending.getValue());
        assertEquals(880, purse.getValue());
    }

    @Test void previousOperationsResponseCannotSettleANewReservation() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var first = request(120); var second = request(150);
        AuctionBidding.stage(pending, purse, first);
        AuctionBidding.settle(pending, purse, new ArrayList<>(), List.of(), first, response(first, false));
        AuctionBidding.stage(pending, purse, second);
        assertThrows(IllegalStateException.class, () -> AuctionBidding.settle(pending, purse, new ArrayList<>(), List.of(), first, response(first, false)));
        assertEquals(850, purse.getValue());
        assertNotNull(pending.getValue().pending());
    }

    @Test void invalidAmountsCannotCreateReservations() {
        for (long amount : new long[]{0, -10, 1001, Long.MAX_VALUE}) {
            var pending = new DatapointAuctionOperations("auction_operations");
            var purse = new DatapointDouble("coins", 1000D);
            assertThrows(IllegalStateException.class, () -> AuctionBidding.stage(pending, purse, request(amount)));
            assertEquals(1000, purse.getValue());
            assertNull(pending.getValue().pending());
        }
    }

    @Test void acceptedRepeatBidDoesNotDuplicateTracking() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        var active = new ArrayList<>(List.of(request.bid().item().getUuid()));
        AuctionBidding.stage(pending, purse, request);
        AuctionBidding.settle(pending, purse, active, List.of(), request, response(request, true));
        assertEquals(1, active.size());
        assertEquals(880, purse.getValue());
    }

    @Test void repeatBidOnlyReservesIncreaseOverPlayersExistingBid() {
        UUID player = UUID.randomUUID();
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 600_000, false, 100L);
        item.setBids(new ArrayList<>(List.of(
                new AuctionItem.Bid(System.currentTimeMillis(), player, 120L),
                new AuctionItem.Bid(System.currentTimeMillis(), UUID.randomUUID(), 150L))));
        var expected = AuctionAddItemProtocol.ExpectedState.of(item);
        item.getBids().add(new AuctionItem.Bid(System.currentTimeMillis(), player, 200L));
        var request = new Request(UUID.randomUUID(), player, UUID.randomUUID(),
                new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS, expected));
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);

        AuctionBidding.stage(pending, purse, request);

        assertEquals(920D, purse.getValue());
        assertEquals(80L, AuctionBidding.amount(request));
    }

    @Test void lateBidAcknowledgementCannotReactivateAnAlreadyCollectedAuction() {
        var pending = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 1000D);
        var request = request(120);
        AuctionBidding.stage(pending, purse, request);
        var active = new ArrayList<UUID>();
        AuctionBidding.settle(pending, purse, active, List.of(request.bid().item().getUuid()), request, response(request, true));
        assertTrue(active.isEmpty());
        assertNull(pending.getValue().pending());
        assertEquals(880, purse.getValue());
    }

    private static DatapointAuctionOperations reload(DatapointAuctionOperations state) {
        var result = new DatapointAuctionOperations("auction_operations");
        result.deserializeValue(state.getSerializedValue());
        return result;
    }
    private static Request request(long amount) {
        UUID player = UUID.randomUUID();
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), System.currentTimeMillis() + 600_000, false, 100L);
        var expected = AuctionAddItemProtocol.ExpectedState.of(item);
        item.setBids(new ArrayList<>(List.of(new AuctionItem.Bid(System.currentTimeMillis(), player, amount))));
        return new Request(UUID.randomUUID(), player, UUID.randomUUID(),
                new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS, expected));
    }
    private static Response response(Request request, boolean success) {
        return new Response(request.operation(), request.bid().item().getUuid(), request.profile(), success, success ? null : "rejected");
    }
}
