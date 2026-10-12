package net.swofty.type.skyblockgeneric.item.tool;

import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolPassivesTest {
    @Test
    void bingoliburGivesOneMagicFindPerLevelCappedAtFifty() {
        assertEquals(0, ToolPassives.bingoliburMagicFind(0));
        assertEquals(37, ToolPassives.bingoliburMagicFind(37));
        assertEquals(50, ToolPassives.bingoliburMagicFind(300));
    }

    @Test
    void zombiePickaxeGivesFiveFortunePerUndeadUpToTwentyFive() {
        assertEquals(0, ToolPassives.zombieMiningFortune(0));
        assertEquals(15, ToolPassives.zombieMiningFortune(3));
        assertEquals(25, ToolPassives.zombieMiningFortune(9));
    }

    @Test
    void parkAxesOnlyGrantFortuneInThePark() {
        assertEquals(50, ToolPassives.parkForagingFortune("JUNGLE_AXE", true));
        assertEquals(100, ToolPassives.parkForagingFortune("TREECAPITATOR_AXE", true));
        assertEquals(0, ToolPassives.parkForagingFortune("TREECAPITATOR_AXE", false));
        assertEquals(0, ToolPassives.parkForagingFortune("IRON_AXE", true));
    }

    @Test
    void drillsGrantTheirPowderBonusOnlyForTheirPowder() {
        assertEquals(20, ToolPassives.powderPercent("MITHRIL_DRILL_1", HotmPowder.MITHRIL));
        assertEquals(40, ToolPassives.powderPercent("MITHRIL_DRILL_2", HotmPowder.MITHRIL));
        assertEquals(0, ToolPassives.powderPercent("MITHRIL_DRILL_2", HotmPowder.GEMSTONE));
        assertEquals(5, ToolPassives.powderPercent("GEMSTONE_DRILL_1", HotmPowder.GEMSTONE));
        assertEquals(20, ToolPassives.powderPercent("GEMSTONE_DRILL_4", HotmPowder.GEMSTONE));
        assertEquals(0, ToolPassives.powderPercent("TITANIUM_DRILL_1", HotmPowder.GLACITE));
    }

    @Test
    void onlyGemstoneDrillsAreRecognised() {
        assertTrue(ToolPassives.isGemstoneDrill("GEMSTONE_DRILL_3"));
        assertFalse(ToolPassives.isGemstoneDrill("MITHRIL_DRILL_1"));
    }
}
