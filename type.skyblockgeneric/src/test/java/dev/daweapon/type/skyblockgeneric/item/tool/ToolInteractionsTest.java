package dev.daweapon.type.skyblockgeneric.item.tool;

import net.minestom.server.instance.Weather;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolInteractionsTest {
    @Test
    void sculptorsAxeTurnsLogsIntoSixSidedWood() {
        assertEquals("minecraft:oak_wood", ToolInteractions.wholeBarkKey("minecraft:oak_log"));
        assertEquals("minecraft:dark_oak_wood", ToolInteractions.wholeBarkKey("minecraft:dark_oak_log"));
        assertEquals("minecraft:stripped_birch_wood", ToolInteractions.wholeBarkKey("minecraft:stripped_birch_log"));
        assertEquals("minecraft:crimson_hyphae", ToolInteractions.wholeBarkKey("minecraft:crimson_stem"));
        assertNull(ToolInteractions.wholeBarkKey("minecraft:oak_wood"));
        assertNull(ToolInteractions.wholeBarkKey("minecraft:stone"));
    }

    @Test
    void weatherWandCyclesClearRainThunder() {
        assertEquals(Weather.RAIN, ToolInteractions.nextWeather(Weather.CLEAR));
        assertEquals(Weather.THUNDER, ToolInteractions.nextWeather(Weather.RAIN));
        assertEquals(Weather.CLEAR, ToolInteractions.nextWeather(Weather.THUNDER));
    }

    @Test
    void throwingAxesAreTheFourAxesWithTheAbility() {
        assertEquals(4, ToolInteractions.THROWING_AXES.size());
        assertTrue(ToolInteractions.isInteractive("TREECAPITATOR_AXE"));
        assertTrue(ToolInteractions.isInteractive("WEATHER_STICK"));
        assertTrue(ToolInteractions.isInteractive("SCULPTORS_AXE"));
        assertFalse(ToolInteractions.isInteractive("FIG_AXE"));
        assertFalse(ToolInteractions.isInteractive(null));
        assertEquals(0.5, ToolInteractions.THROWING_AXE_SWEEP_SCALE);
    }
}
