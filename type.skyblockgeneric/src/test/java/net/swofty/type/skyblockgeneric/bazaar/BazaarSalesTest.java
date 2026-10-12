package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarSalesTest {
    @Test
    void summaryListsTheTenBiggestLinesAndFoldsTheRest() {
        List<BazaarSales.Line> lines = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            lines.add(new BazaarSales.Line(ItemType.COAL, 10 + i, 1000 - i * 10));
        }
        BazaarSales.Summary summary = new BazaarSales.Summary(lines, 0, 12_000);

        assertEquals(BazaarSales.LISTED_LINES, summary.listed().size());
        assertTrue(summary.hasOthers());
        assertEquals(20 + 21 + 22, summary.otherAmount());
        assertEquals(900 + 890 + 880, summary.otherCoins());
    }

    @Test
    void smallSummariesHaveNoOtherLine() {
        BazaarSales.Summary summary = new BazaarSales.Summary(List.of(new BazaarSales.Line(ItemType.COAL, 3, 30)), 1, 30);
        assertFalse(summary.hasOthers());
        assertTrue(summary.hasUnsellable());
        assertFalse(summary.isEmpty());
    }

    @Test
    void nothingToSellIsEmpty() {
        assertTrue(new BazaarSales.Summary(List.of(), 0, 0).isEmpty());
    }
}
