package dev.daweapon.type.skyblockgeneric.enchantment;

import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentCombo;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentWoodsplitter;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentInfiniteQuiver;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentPiercing;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentPunch;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentSnipe;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentSoulEater;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentSwarm;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentWise;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentTitanKiller;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentTripleStrike;
import dev.daweapon.type.skyblockgeneric.item.events.EnchantmentFeatherFallingEvent;
import dev.daweapon.type.skyblockgeneric.item.events.EnchantmentThornsEvent;
import dev.daweapon.type.skyblockgeneric.item.events.EnchantmentTidalEvent;
import dev.daweapon.type.skyblockgeneric.item.components.EnchantableComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentEffectsTest {
    private static String text(String key, int level) {
        return EnchantmentCatalogue.require(key).description(level).replaceAll("<[^>]*>", "");
    }

    private static boolean says(String key, int level, String number) {
        return Pattern.compile("(^|[^0-9.])" + Pattern.quote(number) + "([^0-9]|$)").matcher(text(key, level)).find();
    }

    @Test
    void titanKillerScalesWithDefenseAndCaps() {
        assertEquals(0, EnchantmentTitanKiller.bonus(7, 99));
        assertEquals(20, EnchantmentTitanKiller.bonus(7, 100));
        assertEquals(60, EnchantmentTitanKiller.bonus(7, 350));
        assertEquals(80, EnchantmentTitanKiller.bonus(7, 10_000));
        for (int level = 1; level <= 7; level++) {
            assertTrue(says("TITAN_KILLER", level, (long) EnchantmentTitanKiller.PERCENT_PER_100_DEFENSE[level - 1] + ""), "per 100 at " + level);
            assertTrue(says("TITAN_KILLER", level, (long) EnchantmentTitanKiller.PERCENT_CAP[level - 1] + ""), "cap at " + level);
        }
    }

    @Test
    void tripleStrikeBoostsOnlyTheFirstThreeHits() {
        assertFalse(EnchantmentTripleStrike.boosts(0));
        assertTrue(EnchantmentTripleStrike.boosts(1));
        assertTrue(EnchantmentTripleStrike.boosts(3));
        assertFalse(EnchantmentTripleStrike.boosts(4));
        for (int level = 1; level <= 5; level++)
            assertTrue(says("TRIPLE_STRIKE", level, (long) EnchantmentTripleStrike.PERCENT[level - 1] + ""));
    }

    @Test
    void comboCountsKillsInsideTheWindowAndCapsThem() {
        long now = 100_000;
        assertEquals(0, EnchantmentCombo.bonus(5, List.of(), now));
        assertEquals(9, EnchantmentCombo.bonus(3, List.of(now - 1000, now - 2000, now - 3000), now));
        assertEquals(1, EnchantmentCombo.bonus(1, List.of(now - 500, now - 60_000), now));
        List<Long> many = java.util.stream.LongStream.range(0, 30).map(i -> now - i * 10).boxed().toList();
        assertEquals(5 * 10, EnchantmentCombo.bonus(5, many, now));
        assertEquals(10, EnchantmentCombo.maxKills(5));
        assertEquals(10_000, EnchantmentCombo.windowMillis(5));
        assertTrue(says("COMBO", 5, "10"));
    }

    @Test
    void swarmCapsAtTenEnemies() {
        assertEquals(0, EnchantmentSwarm.bonus(5, 0));
        assertEquals(10, EnchantmentSwarm.bonus(5, 1));
        assertEquals(100, EnchantmentSwarm.bonus(5, 10));
        assertEquals(100, EnchantmentSwarm.bonus(5, 40));
        assertTrue(says("SWARM", 5, "10"));
    }

    @Test
    void woodsplitterMatchesItsText() {
        for (int level = 1; level <= 6; level++) {
            assertTrue(says("WOODSPLITTER", level, (long) EnchantmentWoodsplitter.BONUSES[level - 1] + ""));
        }
    }

    @Test
    void arrowEnchantmentsComputeTheirEffect() {
        assertEquals(1.0, EnchantmentSnipe.multiplier(4, 9.9));
        assertEquals(1.04, EnchantmentSnipe.multiplier(4, 10), 1e-9);
        assertEquals(1.12, EnchantmentSnipe.multiplier(4, 35), 1e-9);
        assertEquals(1.0, EnchantmentPiercing.damageFactor(0));
        assertEquals(0.25, EnchantmentPiercing.damageFactor(2));
        assertEquals(30, EnchantmentInfiniteQuiver.savePercent(10));
        assertEquals(6, EnchantmentPunch.knockback(2) / EnchantmentPunch.KNOCKBACK_PER_LEVEL * 3, 1e-4);
        for (int level = 1; level <= 10; level++)
            assertTrue(says("INFINITE_QUIVER", level, (long) EnchantmentInfiniteQuiver.savePercent(level) + ""));
    }

    @Test
    void featherFallingRemovesHeightAndPercent() {
        assertEquals(0, EnchantmentFeatherFallingEvent.reduce(2, 1), 1e-9);
        assertEquals((20 - 20) * 0.5, EnchantmentFeatherFallingEvent.reduce(20, 10), 1e-9);
        assertEquals((40 - 2 * 3) * 0.85, EnchantmentFeatherFallingEvent.reduce(40, 3), 1e-9);
        assertTrue(says("FEATHER_FALLING", 10, "50"));
    }

    @Test
    void thornsReboundAPercentOfTheDamage() {
        assertEquals(12, EnchantmentThornsEvent.rebound(100, 4), 1e-9);
        assertEquals(3, EnchantmentThornsEvent.rebound(100, 1), 1e-9);
        assertTrue(says("THORNS", 4, "12"));
    }

    @Test
    void tidalLowersSeaCreatureDamage() {
        assertEquals(100, EnchantmentTidalEvent.reduce(100, 0, 3), 1e-9);
        double reduced = EnchantmentTidalEvent.reduce(100, 200, 3);
        assertEquals(100 * 300D / 330D, reduced, 1e-9);
        assertTrue(says("TIDAL", 3, "15"));
    }

    @Test
    void groupsExpandToThePieceTheItemIs() {
        var helmet = EnchantableComponent.expand(List.of(EnchantItemGroups.ARMOR), "DIAMOND_HELMET", "");
        assertTrue(helmet.contains(EnchantItemGroups.HELMET));
        assertFalse(helmet.contains(EnchantItemGroups.BOOTS));
        var head = EnchantableComponent.expand(List.of(EnchantItemGroups.ARMOR), "PLAYER_HEAD", "SOME_HELMET");
        assertTrue(head.contains(EnchantItemGroups.HELMET));
        var boots = EnchantableComponent.expand(List.of(EnchantItemGroups.ARMOR), "PLAYER_HEAD", "ROSEWATER_BOOTS");
        assertTrue(boots.contains(EnchantItemGroups.BOOTS));
        var axe = EnchantableComponent.expand(List.of(EnchantItemGroups.TOOLS), "DIAMOND_AXE", "");
        assertTrue(axe.contains(EnchantItemGroups.AXE));
        var necklace = EnchantableComponent.expand(List.of(EnchantItemGroups.EQUIPMENT), "PLAYER_HEAD", "SOME_NECKLACE");
        assertTrue(necklace.contains(EnchantItemGroups.NECKLACE));
        assertFalse(necklace.contains(EnchantItemGroups.BELT));
        var unknown = EnchantableComponent.expand(List.of(EnchantItemGroups.EQUIPMENT), "PLAYER_HEAD", "SOMETHING");
        assertTrue(unknown.containsAll(List.of(EnchantItemGroups.NECKLACE, EnchantItemGroups.BELT, EnchantItemGroups.GLOVES)));
    }

    @Test
    void wiseCutsManaCostByTenPercentPerLevel() {
        assertEquals(1.0, EnchantmentWise.costMultiplier(0), 1e-9);
        assertEquals(0.5, EnchantmentWise.costMultiplier(5), 1e-9);
        for (int level = 1; level <= 5; level++) assertTrue(says("WISE", level, (level * 10) + ""));
    }

    @Test
    void soulEaterScalesWithLevelAndCaps() {
        assertEquals(200, EnchantmentSoulEater.bonus(1, 100));
        assertEquals(1000, EnchantmentSoulEater.bonus(5, 100));
        assertEquals(EnchantmentSoulEater.MAX_BONUS, EnchantmentSoulEater.bonus(5, 1e9));
        assertEquals(0, EnchantmentSoulEater.bonus(5, -4));
    }

    @Test
    void maximizingPicksTheUltimateThatBelongsToTheItem() {
        var swordGroups = List.of(EnchantItemGroups.SWORD);
        assertEquals(EnchantmentType.WISE, UltimateEnchantChoice.forItem("HYPERION", swordGroups));
        assertEquals(EnchantmentType.CHIMERA, UltimateEnchantChoice.forItem("RAGNAROCK_AXE", List.of(EnchantItemGroups.TOOLS, EnchantItemGroups.AXE, EnchantItemGroups.SWORD)));
        assertEquals(EnchantmentType.CHIMERA, UltimateEnchantChoice.forItem("DAEDALUS_AXE", List.of(EnchantItemGroups.AXE, EnchantItemGroups.SWORD)));
        assertEquals(EnchantmentType.SOUL_EATER, UltimateEnchantChoice.forItem("MIDAS_SWORD", swordGroups));
        assertEquals(EnchantmentType.REITERATE, UltimateEnchantChoice.forItem("TERMINATOR", List.of(EnchantItemGroups.BOW)));
        assertEquals(EnchantmentType.FLOWSTATE, UltimateEnchantChoice.forItem("DIAMOND_PICKAXE", List.of(EnchantItemGroups.PICKAXE)));
        assertEquals(EnchantmentType.FLASH, UltimateEnchantChoice.forItem("ROD", List.of(EnchantItemGroups.FISHING_ROD)));
        assertEquals(EnchantmentType.FIRST_IMPRESSION, UltimateEnchantChoice.forItem("IRON_AXE", List.of(EnchantItemGroups.TOOLS, EnchantItemGroups.AXE)));
        assertEquals(null, UltimateEnchantChoice.forItem("X", List.of()));
    }
}
