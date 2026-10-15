package dev.daweapon.commons.skyblock.auctions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionRulesTest {
    @Test void bidsIncreaseByAtLeastTwoAndAHalfPercentRoundedUp() {
        assertEquals(103, AuctionRules.minimumBid(1, 100L));
        assertEquals(1025, AuctionRules.minimumBid(1, 1000L));
        assertEquals(99, AuctionRules.minimumBid(99, null));
    }

    @Test void wikiDurationFeeAnchorsAreExact() {
        long hour = 3_600_000L;
        assertEquals(20, AuctionRules.durationFee(hour));
        assertEquals(45, AuctionRules.durationFee(6 * hour));
        assertEquals(100, AuctionRules.durationFee(12 * hour));
        assertEquals(350, AuctionRules.durationFee(24 * hour));
        assertEquals(1_200, AuctionRules.durationFee(48 * hour));
        assertEquals(55_200, AuctionRules.durationFee(336 * hour));
        assertThrows(IllegalArgumentException.class, () -> AuctionRules.durationFee(337 * hour));
    }

    @Test void listingFeesAndCollectionTaxUseWikiTiers() {
        long hour = 3_600_000L;
        assertEquals(50_020, AuctionRules.listingFee(1_000_000, hour, false));
        assertEquals(10_020, AuctionRules.listingFee(1_000_000, hour, true));
        assertEquals(200_020, AuctionRules.listingFee(10_000_000, hour, true));
        assertEquals(2_500_020, AuctionRules.listingFee(100_000_000, hour, true));
        assertEquals(0, AuctionRules.collectionTax(1_000_000));
        assertEquals(1, AuctionRules.collectionTax(1_000_001));
        assertEquals(20_000, AuctionRules.collectionTax(2_000_000));
    }

    @Test void derpyQuadruplesOnlyTheCollectionTaxAndKeepsTheOneMillionFloor() {
        assertEquals(80_000, AuctionRules.collectionTax(2_000_000, 4));
        assertEquals(2_000_000_000L, AuctionRules.collectionTax(50_000_000_000L, 4));
        assertEquals(1, AuctionRules.collectionTax(1_000_001, 4));
        assertEquals(20_000, AuctionRules.collectionTax(2_000_000, 0));
    }
}
