package dev.daweapon.type.skyblockgeneric.hotf.throwing;

import dev.daweapon.type.skyblockgeneric.hotf.HotfAbility;
import dev.daweapon.type.skyblockgeneric.hotf.HotfPerk;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRegistry;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxeThrowingPerksTest {
    private final HotfRegistry registry = HotfRegistry.load(Path.of("../configuration/skyblock/hotf"));

    @Test
    void axeTossIsAnAvailableTierTwoAbility() {
        HotfPerk perk = registry.perk("axe_toss");
        assertTrue(perk.isAbility());
        assertTrue(perk.available());
        assertEquals(2, perk.tier());
        assertEquals(1, perk.maxLevel());
    }

    @Test
    void abilityListHasAllThreeAxeAbilitiesWithWikiTimings() {
        assertEquals(3, HotfAbility.values().length);
        for (HotfAbility ability : HotfAbility.values()) assertNotNull(registry.perk(ability.perkId()));
        assertEquals(10, HotfAbility.AXE_TOSS.duration(1));
        assertEquals(120, HotfAbility.AXE_TOSS.cooldown(1));
        assertEquals(115, HotfAbility.AXE_TOSS.cooldown(2));
        assertEquals(100, HotfAbility.AXE_TOSS.cooldown(5));
    }

    @Test
    void homingAxeIsASingleLevelTierSixDesertPerk() {
        HotfPerk perk = registry.perk("homing_axe");
        assertTrue(perk.available());
        assertEquals(6, perk.tier());
        assertEquals(1, perk.maxLevel());
        assertEquals(2, perk.lore().size());
    }

    @Test
    void ricochetCostsFollowTheWikiFormulaAndChanceIsOnePercentPerLevel() {
        HotfPerk perk = registry.perk("ricochet");
        assertTrue(perk.available());
        assertEquals(7, perk.tier());
        assertEquals(10, perk.maxLevel());
        assertEquals((long) Math.floor(Math.pow(3, 5.5)), perk.costToReach(2));
        assertEquals((long) Math.floor(Math.pow(11, 5.5)), perk.costToReach(10));
        assertEquals(10, perk.value(10));
        assertEquals(1, perk.value(1));
    }

    @Test
    void bounceChanceIsMechanical() {
        assertFalse(ThrowTargeting.bounces(0, 0));
        assertTrue(ThrowTargeting.bounces(10, 0.0999));
        assertFalse(ThrowTargeting.bounces(10, 0.1));
    }

    @Test
    void nearestLogSkipsExcludedAndTooCloseLogs() {
        Set<Point> logs = Set.of(new BlockVec(1, 0, 0), new BlockVec(5, 0, 0), new BlockVec(9, 0, 0));
        Point found = ThrowTargeting.nearestLog(new Vec(0, 0, 0), 10, 3, logs::contains, point -> point.blockX() == 5);
        assertEquals(9, found.blockX());
        assertNull(ThrowTargeting.nearestLog(new Vec(0, 0, 0), 4, 3, logs::contains, point -> false));
    }
}
