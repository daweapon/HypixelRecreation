package dev.daweapon.type.skyblockgeneric.item.armor;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WitherArmorTest {
    private static final List<String> NECRON = List.of("POWER_WITHER_HELMET", "POWER_WITHER_CHESTPLATE",
            "POWER_WITHER_LEGGINGS", "POWER_WITHER_BOOTS");

    @Test
    void everyFloorSevenSetCountsAsWitherArmor() {
        for (String variant : List.of("WITHER", "SPEED_WITHER", "WISE_WITHER", "TANK_WITHER", "POWER_WITHER")) {
            for (String slot : List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS")) {
                assertTrue(WitherArmor.isPiece(variant + "_" + slot), variant + "_" + slot);
            }
        }
        assertFalse(WitherArmor.isPiece("WITHER_GOGGLES"));
        assertFalse(WitherArmor.isPiece("WITHER_CLOAK_SWORD"));
        assertFalse(WitherArmor.isPiece("SHADOW_ASSASSIN_HELMET"));
        assertFalse(WitherArmor.isPiece(""));
        assertFalse(WitherArmor.isPiece(null));
    }

    @Test
    void witherbornNeedsFourPiecesAndMixedSetsCount() {
        assertTrue(WitherArmor.witherborn(NECRON));
        assertTrue(WitherArmor.witherborn(List.of("POWER_WITHER_HELMET", "WISE_WITHER_CHESTPLATE", "TANK_WITHER_LEGGINGS", "SPEED_WITHER_BOOTS")));
        assertFalse(WitherArmor.witherborn(List.of("WARDEN_HELMET", "POWER_WITHER_CHESTPLATE", "POWER_WITHER_LEGGINGS", "POWER_WITHER_BOOTS")));
        assertFalse(WitherArmor.witherborn(List.of("", "", "", "")));
    }

    @Test
    void eachPieceCutsWitherDamageByTenPercent() {
        assertEquals(1.0, WitherArmor.witherDamageMultiplier(0), 1e-9);
        assertEquals(0.9, WitherArmor.witherDamageMultiplier(1), 1e-9);
        assertEquals(0.6, WitherArmor.witherDamageMultiplier(WitherArmor.pieces(NECRON)), 1e-9);
    }

    @Test
    void onlyMaxorsPiecesAddFivePercentArrowDamageEach() {
        List<String> mixed = List.of("SPEED_WITHER_HELMET", "SPEED_WITHER_CHESTPLATE", "POWER_WITHER_LEGGINGS", "SPEED_WITHER_BOOTS");
        assertEquals(3, WitherArmor.maxorPieces(mixed));
        assertEquals(1.15, WitherArmor.arrowDamageMultiplier(3), 1e-9);
        assertEquals(1.0, WitherArmor.arrowDamageMultiplier(WitherArmor.maxorPieces(NECRON)), 1e-9);
    }

    @Test
    void theSetBonusHeaderShowsTheWornCount() {
        String header = "§6Full Set Bonus: Witherborn §7(0/4)";
        assertEquals("§6Full Set Bonus: Witherborn §7(3/4)", WitherArmor.withWornCount(header, 3));
        assertEquals("§6Full Set Bonus: Witherborn §7(4/4)", WitherArmor.withWornCount(header, 4));
        assertEquals("§7withers by §c10%§7.", WitherArmor.withWornCount("§7withers by §c10%§7.", 4));
    }

    @Test
    void aMinionComesEveryThirtySecondsButNeverTwoAtOnce() {
        WitherbornCycle cycle = new WitherbornCycle();
        assertFalse(cycle.tick(0, 0));
        assertFalse(cycle.tick(29_999, 0));
        assertTrue(cycle.tick(30_000, 0));
        assertFalse(cycle.tick(60_000, 1), "one is still out");
        assertTrue(cycle.tick(90_000, 0));
        cycle.reset();
        assertFalse(cycle.tick(90_500, 0), "taking the set off restarts the wait");
        assertTrue(cycle.tick(120_500, 0));
    }

    @Test
    void theExplosionMessageMatchesHypixel() {
        assertEquals("<7>Your Witherborn hit <c>5 <7>enemies for <c>2,079,000 <7>damage.", WitherbornMinion.message(5, 2_079_000));
        assertEquals("<7>Your Witherborn hit <c>1 <7>enemy for <c>415,800 <7>damage.", WitherbornMinion.message(1, 415_800));
    }

    @Test
    @SuppressWarnings("unchecked")
    void noWitherPieceSaysItsBonusIsInactive() throws Exception {
        for (String file : List.of("misc_d.yml", "misc_e.yml", "misc_f.yml")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils.loadYaml(
                    new File("../configuration/skyblock/items/catalogue/" + file)).get("items");
            for (Map<String, Object> item : items) {
                if (!WitherArmor.isPiece(String.valueOf(item.get("id")))) continue;
                List<String> lore = (List<String>) item.get("lore");
                assertTrue(lore.stream().noneMatch(line -> line.contains("Not active yet")), item.get("id") + " still says not active");
                assertTrue(lore.stream().anyMatch(line -> line.contains(WitherArmor.SET_BONUS_LINE + " §7(0/4)")), String.valueOf(item.get("id")));
            }
        }
    }
}
