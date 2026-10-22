package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconDataTest {
    private static final long NOW = 1_000_000L;
    private static final Path CATALOGUE = Path.of("../configuration/skyblock/items/catalogue/misc_a.yml");

    @Test
    void placedBeaconHasNoPower() {
        BeaconData data = BeaconData.placed(3, 1, 2, 3);
        assertFalse(data.isPowered(NOW));
        assertEquals(0, data.minionSpeedPercent(NOW));
        assertEquals(0, data.profileStatValue(NOW));
    }

    @Test
    void crystalsStackFortyEightHoursEach() {
        BeaconData data = BeaconData.placed(1, 0, 0, 0).withCrystals(1, false, NOW).withCrystals(2, false, NOW);
        assertEquals(3 * BeaconData.POWER_MILLIS, data.remainingMillis(NOW));
    }

    @Test
    void crystalsAddedLaterExtendFromTheRemainingTime() {
        BeaconData first = BeaconData.placed(1, 0, 0, 0).withCrystals(1, false, NOW);
        BeaconData second = first.withCrystals(1, false, NOW + BeaconData.POWER_MILLIS / 2);
        assertEquals(BeaconData.POWER_MILLIS * 2 - BeaconData.POWER_MILLIS / 2, second.remainingMillis(NOW + BeaconData.POWER_MILLIS / 2));
    }

    @Test
    void expiredBeaconGivesNothing() {
        BeaconData data = BeaconData.placed(5, 0, 0, 0).withStat(BeaconStat.MAGIC_FIND).withCrystals(1, false, NOW);
        long later = NOW + BeaconData.POWER_MILLIS + 1;
        assertFalse(data.isPowered(later));
        assertEquals(0, data.profileStatValue(later));
        assertEquals(0, data.minionSpeedPercent(later));
    }

    @Test
    void minionBuffIsTwoPercentPerTierPlusOneForScorched() {
        for (int tier = 1; tier <= 5; tier++) {
            BeaconData normal = BeaconData.placed(tier, 0, 0, 0).withCrystals(1, false, NOW);
            BeaconData scorched = BeaconData.placed(tier, 0, 0, 0).withCrystals(1, true, NOW);
            assertEquals(2.0 * tier, normal.minionSpeedPercent(NOW));
            assertEquals(2.0 * tier + 1, scorched.minionSpeedPercent(NOW));
        }
    }

    @Test
    void profileStatScalesLinearlyWithTierAndScorchedAddsTwentyPercent() {
        BeaconData tierFive = BeaconData.placed(5, 0, 0, 0).withStat(BeaconStat.HEALTH).withCrystals(1, false, NOW);
        assertEquals(50, tierFive.profileStatValue(NOW));
        assertEquals(60, tierFive.withCrystals(1, true, NOW).profileStatValue(NOW));
        assertEquals(2.5, BeaconData.placed(5, 0, 0, 0).withStat(BeaconStat.TRACKING).withCrystals(1, false, NOW).profileStatValue(NOW));
    }

    @Test
    void scorchedBonusEndsWhenScorchedPowerRunsOut() {
        BeaconData data = BeaconData.placed(2, 0, 0, 0).withCrystals(1, true, NOW).withCrystals(1, false, NOW);
        long afterScorched = NOW + BeaconData.POWER_MILLIS + 1;
        assertTrue(data.isScorched(NOW));
        assertTrue(data.isPowered(afterScorched));
        assertFalse(data.isScorched(afterScorched));
    }

    @Test
    void effectsNeedTheRightTierAndPower() {
        BeaconData data = BeaconData.placed(2, 0, 0, 0).withEffectToggled(BeaconEffect.SPEED)
                .withEffectToggled(BeaconEffect.JUMP_BOOST).withEffectToggled(BeaconEffect.STRENGTH);
        assertTrue(data.activeEffects(NOW).isEmpty());
        assertEquals(Set.of(BeaconEffect.SPEED, BeaconEffect.JUMP_BOOST), data.withCrystals(1, false, NOW).activeEffects(NOW));
    }

    @Test
    void togglingAnEffectTwiceTurnsItOff() {
        BeaconData data = BeaconData.placed(5, 0, 0, 0).withEffectToggled(BeaconEffect.HASTE).withEffectToggled(BeaconEffect.HASTE);
        assertTrue(data.effects().isEmpty());
    }

    @Test
    void serializationRoundTrips() {
        BeaconData data = BeaconData.placed(4, -5, 70, 12).withStat(BeaconStat.CRITICAL_DAMAGE)
                .withEffectToggled(BeaconEffect.SPEED).withEffectToggled(BeaconEffect.DEFENSE).withCrystals(3, true, NOW);
        assertEquals(data, BeaconData.deserialize(data.serialize()));
        assertEquals(BeaconData.placed(1, 0, 0, 0), BeaconData.deserialize(BeaconData.placed(1, 0, 0, 0).serialize()));
    }

    @Test
    void garbageDeserializesToNothing() {
        assertNull(BeaconData.deserialize(""));
        assertNull(BeaconData.deserialize(null));
        assertNull(BeaconData.deserialize("1|2|3"));
        assertNull(BeaconData.deserialize("x|0|0|0|0|0||"));
    }

    @Test
    void statTableMatchesTheWiki() {
        assertEquals(50, BeaconStat.HEALTH.valueAtTier(5));
        assertEquals(20, BeaconStat.DEFENSE.valueAtTier(5));
        assertEquals(10, BeaconStat.STRENGTH.valueAtTier(5));
        assertEquals(15, BeaconStat.SPEED.valueAtTier(5));
        assertEquals(25, BeaconStat.INTELLIGENCE.valueAtTier(5));
        assertEquals(5, BeaconStat.ABILITY_DAMAGE.valueAtTier(5));
        assertEquals(12, BeaconStat.values().length);
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyBeaconTierIsPlaceableAndAvailable() throws Exception {
        List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils.loadYaml(CATALOGUE.toFile()).get("items");
        for (int tier = 1; tier <= 5; tier++) {
            String id = "BEACON_" + tier;
            Map<String, Object> item = items.stream().filter(candidate -> id.equals(candidate.get("id"))).findFirst().orElseThrow();
            assertNull(item.get("unavailable"), id + " should be available");
            List<Map<String, Object>> components = (List<Map<String, Object>>) item.get("components");
            assertTrue(components.stream().anyMatch(c -> "PLACEABLE".equals(c.get("id")) && "BEACON".equals(c.get("block_type"))), id + " is not placeable");
        }
    }
}
