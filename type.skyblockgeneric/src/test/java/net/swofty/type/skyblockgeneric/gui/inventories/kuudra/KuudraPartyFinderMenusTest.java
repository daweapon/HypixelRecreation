package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KuudraPartyFinderMenusTest {
    @Test
    void theTierFilterCyclesThroughAnyAndEveryTier() {
        assertEquals(KuudraTier.BASIC, GUIKuudraPartyFinderSearch.cycle(null, false));
        assertEquals(KuudraTier.HOT, GUIKuudraPartyFinderSearch.cycle(KuudraTier.BASIC, false));
        assertNull(GUIKuudraPartyFinderSearch.cycle(KuudraTier.INFERNAL, false));
        assertEquals(KuudraTier.INFERNAL, GUIKuudraPartyFinderSearch.cycle(null, true));
        assertNull(GUIKuudraPartyFinderSearch.cycle(KuudraTier.BASIC, true));
    }

    @Test
    void theCombatRangeIsReadFromASign() {
        assertArrayEquals(new int[]{20, 45}, GUIKuudraPartyFinderSearch.parseRange("20-45"));
        assertArrayEquals(new int[]{20, 45}, GUIKuudraPartyFinderSearch.parseRange(" 45 - 20 "));
        assertArrayEquals(new int[]{30, 60}, GUIKuudraPartyFinderSearch.parseRange("30"));
        assertArrayEquals(new int[]{0, 60}, GUIKuudraPartyFinderSearch.parseRange(""));
        assertArrayEquals(new int[]{10, 60}, GUIKuudraPartyFinderSearch.parseRange("10-99"));
        assertNull(GUIKuudraPartyFinderSearch.parseRange("lots"));
    }

    @Test
    void theRequiredCombatLevelIsReadFromASign() {
        assertEquals(35, GUIKuudraGroupBuilder.parseLevel("35", 0));
        assertEquals(60, GUIKuudraGroupBuilder.parseLevel("200", 0));
        assertEquals(12, GUIKuudraGroupBuilder.parseLevel("abc", 12));
    }
}
