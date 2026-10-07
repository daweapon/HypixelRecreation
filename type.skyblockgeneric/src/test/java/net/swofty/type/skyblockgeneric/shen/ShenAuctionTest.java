package net.swofty.type.skyblockgeneric.shen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShenAuctionTest {
    @Test
    void winnerCountsMatchTheWiki() {
        assertEquals(20, ShenAuctionItem.ARTIFACT_OF_CONTROL.winners(false, false));
        assertEquals(10, ShenAuctionItem.ARTIFACT_OF_CONTROL.winners(true, false));
        assertEquals(160, ShenAuctionItem.SHENS_REGALIA.winners(false, true));
        assertEquals(10, ShenAuctionItem.PRICELESS_THE_FISH.winners(false, true));
        assertEquals(20, ShenAuctionItem.PUZZLE_CUBE.winners(false, true));
    }

    @Test
    void everyServerPicksTheSameTwoSpecialsEachYear() {
        ShenAuction year = new ShenAuction(412);
        List<ShenAuctionItem> specials = year.specials();
        assertEquals(2, specials.size());
        assertEquals(specials, new ShenAuction(412).specials());
        assertTrue(specials.stream().allMatch(item -> item.getSection() == ShenAuctionItem.Section.SPECIAL));
        assertEquals(2, year.volumeTradingSpecials().size());
    }

    @Test
    void volumeTradingItemsOnlyAppearUnderDiaz() {
        ShenAuction year = new ShenAuction(412);
        ShenAuctionItem extra = year.volumeTradingSpecials().getFirst();
        assertFalse(year.offers(extra, false));
        assertTrue(year.offers(extra, true));
        assertTrue(year.offers(ShenAuctionItem.PANDORAS_BOX, false));
    }
}
