package dev.daweapon.type.skyblockgeneric.auction;

import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import java.util.Comparator;
import java.util.UUID;

public final class AuctionBidRefund {
    private AuctionBidRefund() {}

    public static long amount(AuctionItem item, UUID bidder) {
        long committed = 0;
        for (var bid : item.getBids()) {
            if (bidder.equals(bid.uuid())) committed = Math.max(committed, bid.value());
        }
        var winner = item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).orElse(null);
        return winner != null && bidder.equals(winner.uuid()) ? 0 : committed;
    }
}
