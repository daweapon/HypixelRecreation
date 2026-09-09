package net.swofty.service.auction.endpoints;

import net.minestom.server.item.Material;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuctionBidUpdateTest {
    @Test
    void currentHighestBidderCanIncreaseTheirBid() {
        UUID seller = UUID.randomUUID();
        UUID bidder = UUID.randomUUID();
        var item = new AuctionItem(new UnderstandableSkyBlockItem(null, List.of(), 1, Material.STONE), seller,
                System.currentTimeMillis() + 600_000, false, 1_000L);
        item.setBids(new ArrayList<>(List.of(new AuctionItem.Bid(System.currentTimeMillis(), bidder, 1_000L))));
        Document stored = item.toDocument();
        var expected = AuctionAddItemProtocol.ExpectedState.of(item);
        item.getBids().add(new AuctionItem.Bid(System.currentTimeMillis(), bidder, 1_025L));

        assertNotNull(EndpointAddItem.bidUpdate(
                new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS, expected),
                stored, System.currentTimeMillis()));
    }
}
