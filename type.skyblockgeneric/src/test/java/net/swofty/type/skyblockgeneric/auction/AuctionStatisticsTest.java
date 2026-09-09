package net.swofty.type.skyblockgeneric.auction;

import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionOperations;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics.AuctionStatistics.AuctionStat.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuctionStatisticsTest {
    @BeforeAll static void attributes() { ItemAttribute.registerItemAttributes(); }

    @Test void creationCountsOnceWithItsActualFee() {
        var item = new SkyBlockItem(Material.STONE);
        var auction = new AuctionItem(item.toUnderstandable(), UUID.randomUUID(), System.currentTimeMillis() + 60_000, true, 500L);
        var message = new AuctionAddItemProtocol.AuctionAddItemMessage(auction, AuctionCategories.TOOLS);
        var escrow = new DatapointAuctionEscrow.AuctionEscrow(item, 500, 60_000, true);
        var purse = new DatapointDouble("coins", 1_000D);
        var statistics = statistics();
        AuctionCreation.stage(escrow, purse, message, 25);

        var response = new AuctionAddItemProtocol.AuctionAddItemResponse(auction.getUuid(), true, null);
        AuctionCreation.settle(escrow, purse, new ArrayList<>(), statistics, response);
        AuctionCreation.settle(escrow, purse, new ArrayList<>(), statistics, response);

        assertEquals(1L, statistics.get(AUCTIONS_CREATED));
        assertEquals(25L, statistics.get(COINS_SPENT_ON_FEES));
    }

    @Test void acceptedNormalBidsCountButBinPurchasesDoNot() {
        var statistics = statistics();
        settleBid(statistics, false, 750);
        settleBid(statistics, false, 1_250);
        settleBid(statistics, true, 2_000);

        assertEquals(2L, statistics.get(TOTAL_BIDS));
        assertEquals(1_250L, statistics.get(HIGHEST_BID_MADE));
    }

    @Test void durableClaimsUpdateSellerAndWinnerTotalsOnce() {
        String item = new SkyBlockItem(Material.STONE).toUnderstandable().serialize();
        var sellerStats = statistics();
        var buyerStats = statistics();
        var auction = UUID.randomUUID();
        var sellerReceipt = receipt(auction, true, 3_000, null, item);
        var buyerReceipt = receipt(auction, false, 3_000, item, item);

        assertEquals(true, settleClaim(sellerReceipt, sellerStats));
        assertEquals(true, settleClaim(buyerReceipt, buyerStats));

        assertEquals(1L, sellerStats.get(AUCTIONS_COMPLETED_WITH_BIDS));
        assertEquals(3_000L, sellerStats.get(HIGHEST_AUCTION_HELD));
        assertEquals(3_000L, sellerStats.get(TOTAL_COINS_EARNED));
        assertEquals(1L, sellerStats.get(COMMON_SOLD));
        assertEquals(1L, buyerStats.get(AUCTIONS_WON));
        assertEquals(3_000L, buyerStats.get(TOTAL_COINS_SPENT));
        assertEquals(1L, buyerStats.get(COMMON_BOUGHT));
    }

    @Test void unsoldSellerClaimCountsWithoutBid() {
        var statistics = statistics();
        String item = new SkyBlockItem(Material.STONE).toUnderstandable().serialize();
        var receipt = new AuctionClaimProtocol.Receipt(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), true, 0, item, 0, false, item);

        settleClaim(receipt, statistics);

        assertEquals(1L, statistics.get(AUCTIONS_COMPLETED_WITHOUT_BIDS));
        assertEquals(0L, statistics.get(COMMON_SOLD));
    }

    @Test void coinStatisticsRoundTripBeyondIntegerRange() {
        var datapoint = new DatapointAuctionStatistics("auction_statistics", statistics());
        datapoint.getValue().set(TOTAL_COINS_EARNED, 5_000_000_000L);

        var restored = datapoint.getSerializer().deserialize(datapoint.getSerializedValue());

        assertEquals(5_000_000_000L, restored.get(TOTAL_COINS_EARNED));
    }

    private static void settleBid(DatapointAuctionStatistics.AuctionStatistics statistics, boolean bin, long amount) {
        var auction = new AuctionItem(new SkyBlockItem(Material.STONE).toUnderstandable(), UUID.randomUUID(),
                System.currentTimeMillis() + 60_000, bin, amount);
        var expected = AuctionAddItemProtocol.ExpectedState.of(auction);
        UUID player = UUID.randomUUID();
        auction.setBids(new ArrayList<>(List.of(new AuctionItem.Bid(System.currentTimeMillis(), player, amount))));
        var request = new AuctionBidProtocol.Request(UUID.randomUUID(), player, UUID.randomUUID(),
                new AuctionAddItemProtocol.AuctionAddItemMessage(auction, AuctionCategories.TOOLS, expected));
        var operations = new DatapointAuctionOperations("auction_operations");
        var purse = new DatapointDouble("coins", 10_000D);
        AuctionBidding.stage(operations, purse, request);
        AuctionBidding.settle(operations, purse, new ArrayList<>(), List.of(), statistics, request,
                new AuctionBidProtocol.Response(request.operation(), auction.getUuid(), request.profile(), true, null));
    }

    private static AuctionClaimProtocol.Receipt receipt(UUID auction, boolean seller, long price, String reward, String item) {
        return new AuctionClaimProtocol.Receipt(UUID.randomUUID(), auction, UUID.randomUUID(), UUID.randomUUID(),
                seller, seller ? price : 0, reward, price, true, item);
    }

    private static boolean settleClaim(AuctionClaimProtocol.Receipt receipt,
                                       DatapointAuctionStatistics.AuctionStatistics statistics) {
        var active = new ArrayList<>(List.of(receipt.auction()));
        return AuctionClaims.settle(receipt, new DatapointDouble("coins", 0D), new ArrayList<>(), active,
                new ArrayList<>(), statistics, () -> {}, () -> {});
    }

    private static DatapointAuctionStatistics.AuctionStatistics statistics() {
        return new DatapointAuctionStatistics("auction_statistics").getValue();
    }
}
