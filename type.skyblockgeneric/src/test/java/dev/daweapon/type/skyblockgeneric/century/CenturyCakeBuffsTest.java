package dev.daweapon.type.skyblockgeneric.century;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CenturyCakeBuffsTest {
    private static final long NOW = 1_000_000_000L;

    @Test
    void aBuffIsActiveUntilItsExpiry() {
        Map<String, Long> state = Map.of(CenturyCakeBuffs.BUFF_PREFIX + CenturyCake.RED.name(), NOW + 1);
        assertTrue(CenturyCakeBuffs.isActive(state, CenturyCake.RED, NOW));
        assertFalse(CenturyCakeBuffs.isActive(state, CenturyCake.RED, NOW + 1));
        assertFalse(CenturyCakeBuffs.isActive(state, CenturyCake.BLUE, NOW));
    }

    @Test
    void soulsNeedAnEightHourProfileAndNoActiveBuff() {
        long eightHours = CenturyCakeBuffs.SOUL_MIN_PROFILE_AGE_MS;
        assertTrue(CenturyCakeBuffs.soulEligible(false, eightHours + 1));
        assertFalse(CenturyCakeBuffs.soulEligible(false, eightHours));
        assertFalse(CenturyCakeBuffs.soulEligible(true, eightHours * 10));
    }

    @Test
    void cakesMatchTheWikiTable() {
        assertEquals(20, CenturyCake.values().length);
        assertEquals(ItemStatistic.HEALTH, CenturyCake.PINK.statistic());
        assertEquals(10, CenturyCake.PINK.amount());
        assertEquals(ItemStatistic.MINING_FORTUNE, CenturyCake.CYAN.statistic());
        assertEquals(CakeSoulColour.CYAN, CenturyCake.CYAN.soulColour());
        assertEquals(CakeSoulColour.AQUA, CenturyCake.AQUA.soulColour());
        assertEquals(CakeSoulColour.LIGHT_GRAY, CenturyCake.SILVER.soulColour());
        assertNull(CenturyCake.MAGENTA.statistic());
        assertEquals("<c>+2 <c>Strength", CenturyCake.RED.bonus());
        assertEquals(CenturyCake.SEVEN_SEAS, CenturyCake.fromItemId("EPOCH_CAKE_SEVEN_SEAS"));
        assertNull(CenturyCake.fromItemId("CAKE_COUNTER"));
    }

    @Test
    void cakeSoulDataKeepsColourAndOwner() {
        String withOwner = CakeSoul.encode(CakeSoulColour.LIGHT_GRAY, "Minikloon");
        assertEquals(CakeSoulColour.LIGHT_GRAY, CakeSoul.colourOf(withOwner));
        assertEquals("Minikloon", CakeSoul.ownerOf(withOwner));
        String noOwner = CakeSoul.encode(CakeSoulColour.PINK, null);
        assertEquals(CakeSoulColour.PINK, CakeSoul.colourOf(noOwner));
        assertNull(CakeSoul.ownerOf(noOwner));
        assertNull(CakeSoul.colourOf(""));
    }
}
