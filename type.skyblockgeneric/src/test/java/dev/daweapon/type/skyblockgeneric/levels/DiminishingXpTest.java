package dev.daweapon.type.skyblockgeneric.levels;

import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.PowderExperience;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiminishingXpTest {
    private static final DiminishingXp MITHRIL = new DiminishingXp(2_400, 600_000, 12_500_000, 620);

    @Test
    void oneXpPerStepUpToTheLinearCap() {
        assertEquals(0, MITHRIL.experience(0));
        assertEquals(1, MITHRIL.experience(2_400), 1e-9);
        assertEquals(250, MITHRIL.experience(600_000), 1e-9);
    }

    @Test
    void theTailShrinksAndIsExactlyFullAtTheUpperCap() {
        assertTrue(MITHRIL.experience(1_000_000) > 250);
        double previous = MITHRIL.experience(1_000_000);
        double previousStep = Double.MAX_VALUE;
        for (long amount = 2_000_000; amount <= 12_000_000; amount += 1_000_000) {
            double xp = MITHRIL.experience(amount);
            assertTrue(xp > previous, "more powder never gives less XP");
            assertTrue(xp - previous < previousStep, "each million gives less than the one before");
            previousStep = xp - previous;
            previous = xp;
        }
        assertEquals(620, MITHRIL.experience(12_500_000));
        assertEquals(620, MITHRIL.experience(2_000_000_000L));
    }

    @Test
    void powderTasksAddUpToTheWikisHeartOfTheMountainPowderTotal() {
        double total = Arrays.stream(HotmPowder.values()).mapToDouble(PowderExperience::maxXp).sum();
        assertEquals(1_942, total);
        assertEquals(661, PowderExperience.experience(HotmPowder.GEMSTONE, 20_000_000));
        assertEquals(240, PowderExperience.experience(HotmPowder.GLACITE, 600_000));
        assertEquals(Math.floor(MITHRIL.experience(700_000)), PowderExperience.experience(HotmPowder.MITHRIL, 700_000),
                "powder XP is whole numbers only");
    }
}
