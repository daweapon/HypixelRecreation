package dev.daweapon.type.skyblockgeneric.auction;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionBidRefundTest {
    private final UUID first = UUID.randomUUID(), second = UUID.randomUUID();
    private AuctionItem auction(long... values) {
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE),
                UUID.randomUUID(), 1, false, 100L);
        var bids = new ArrayList<AuctionItem.Bid>();
        for (int i = 0; i < values.length; i++) bids.add(new AuctionItem.Bid((long) i, i % 2 == 0 ? first : second, values[i]));
        item.setBids(bids);
        return item;
    }
    @Test void repeatLoserRecoversLatestCommittedBid() {
        var item = auction(100, 150, 200, 250);
        assertEquals(200, AuctionBidRefund.amount(item, first));
        assertEquals(0, AuctionBidRefund.amount(item, second));
    }
    @Test void repeatWinnerPaysOnlyWinningPrice() {
        var item = auction(100, 150, 200);
        assertEquals(0, AuctionBidRefund.amount(item, first));
        assertEquals(150, AuctionBidRefund.amount(item, second));
    }
    @Test void refundsAndSellerProceedsEqualAllCharges() {
        var item = auction(100, 150, 200, 250, 300, 350);
        assertEquals(650, AuctionBidRefund.amount(item, first) + AuctionBidRefund.amount(item, second) + 350);
    }
    @Test void singleBidAndNonParticipantHaveNoExcess() {
        var item = auction(100);
        assertEquals(0, AuctionBidRefund.amount(item, first));
        assertEquals(0, AuctionBidRefund.amount(item, second));
        assertEquals(0, AuctionBidRefund.amount(auction(), first));
    }
    @Test void legacySerializedHistoryRetainsRefundAmounts() {
        var item = AuctionItem.fromDocument(auction(100, 150, 200, 250).toDocument());
        assertEquals(200, AuctionBidRefund.amount(item, first));
        assertEquals(0, AuctionBidRefund.amount(item, second));
    }
}
