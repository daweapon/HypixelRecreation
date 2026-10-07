package net.swofty.type.skyblockgeneric.stonks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StonksAuctionTierTest {

    @Test
    void placementsMapToTheWikiRewardTiers() {
        assertEquals(25, StonksAuctionTier.forPlacement(1).getStocks());
        assertEquals(25, StonksAuctionTier.forPlacement(100).getStocks());
        assertEquals(15, StonksAuctionTier.forPlacement(101).getStocks());
        assertEquals(10, StonksAuctionTier.forPlacement(1_000).getStocks());
        assertEquals(5, StonksAuctionTier.forPlacement(2_500).getStocks());
        assertEquals(2, StonksAuctionTier.forPlacement(5_000).getStocks());
        assertEquals(1, StonksAuctionTier.forPlacement(5_001).getStocks());
    }

    @Test
    void unrankedBiddersStillGetOneStock() {
        assertEquals(StonksAuctionTier.EVERYONE_ELSE, StonksAuctionTier.forPlacement(-1));
    }
}
