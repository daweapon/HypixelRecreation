package dev.daweapon.type.skyblockgeneric.gui.inventories.hotm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotmTooltipsTest {
    @Test
    void longLinesWrapAndKeepTheirColour() {
        String wrapped = HotmTooltips.wrap("<7>You have unlocked this tier. All perks and abilities on this tier are available for unlocking with <5>Token of the Mountain<7>.");
        String[] lines = wrapped.split("\n");
        assertTrue(lines.length >= 3, wrapped);
        for (String line : lines) assertTrue(line.replaceAll("<[^>]*>", "").length() <= 38, line);
        assertTrue(lines[lines.length - 1].startsWith("<5>") || lines[lines.length - 1].contains("<5>"), wrapped);
    }

    @Test
    void shortLinesAreUntouched() {
        assertEquals("<a>UNLOCKED\n<7>Tier 5", HotmTooltips.wrap("<a>UNLOCKED\n<7>Tier 5"));
    }
}
