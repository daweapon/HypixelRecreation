package dev.daweapon.type.skyblockgeneric.pity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PityProgressTest {
    @Test
    void belowMaxIsRedWithGroupedNumbers() {
        assertEquals("<7>Progress: <c>0<7>/<a>2,000", PityProgress.line("Progress", 0, 2000));
    }

    @Test
    void atOrOverMaxIsGreenAndCapped() {
        assertEquals("<7>Progress: <a>750<7>/<a>750", PityProgress.line("Progress", 900, 750));
    }

    @Test
    void percentTrimsTrailingZeros() {
        assertEquals("0.05", PityProgress.percent(0.05));
        assertEquals("50", PityProgress.percent(50));
        assertEquals("<7>Progress from Crops: <c>0%<7>/<a>100%", PityProgress.percentLine("Progress from Crops", 0));
    }

    @Test
    void everyCategoryHasItemsInsideTheChest() {
        assertEquals(9, PityCatalogue.categories().size());
        for (PityCategory category : PityCatalogue.categories()) {
            assertTrue(category.slot() < 54);
            assertFalse(category.items().isEmpty());
            category.items().forEach(item -> assertTrue(item.slot() < 48 || item.slot() > 49));
        }
    }
}
