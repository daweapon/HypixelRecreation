package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.EnchantLevelBoost;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantLevelBoostTest {
    @Test
    void idolRaisesEnderSlayerSixToSeven() {
        Map<EnchantmentType, Integer> levels = Map.of(EnchantmentType.ENDER_SLAYER, 6, EnchantmentType.POWER, 6);
        assertTrue(EnchantLevelBoost.ENDSTONE_IDOL.appliesTo(levels));
        assertEquals(7, EnchantLevelBoost.ENDSTONE_IDOL.boosted(levels).get(EnchantmentType.ENDER_SLAYER));
        assertEquals(6, EnchantLevelBoost.ENDSTONE_IDOL.boosted(levels).get(EnchantmentType.POWER));
    }

    @Test
    void boostCanOnlyBeAppliedOnce() {
        Map<EnchantmentType, Integer> applied = EnchantLevelBoost.ENSNARED_SNAIL.boosted(Map.of(EnchantmentType.BANE_OF_ARTHROPODS, 6));
        assertFalse(EnchantLevelBoost.ENSNARED_SNAIL.appliesTo(applied));
    }

    @Test
    void wrongLevelOrMissingEnchantmentIsRefused() {
        assertFalse(EnchantLevelBoost.DISTANT_ECHO.appliesTo(Map.of(EnchantmentType.STEALTH, 4)));
        assertFalse(EnchantLevelBoost.DISTANT_ECHO.appliesTo(Map.of(EnchantmentType.POWER, 5)));
        assertTrue(EnchantLevelBoost.DISTANT_ECHO.appliesTo(Map.of(EnchantmentType.STEALTH, 5)));
    }

    @Test
    void endcapUpgradesRaiseTheirEnchantmentByOneLevel() {
        assertEquals(7, EnchantLevelBoost.TROUBLED_BUBBLE.boosted(Map.of(EnchantmentType.PISCARY, 6)).get(EnchantmentType.PISCARY));
        assertEquals(6, EnchantLevelBoost.VIBRANT_CORAL.boosted(Map.of(EnchantmentType.SCUBA, 5)).get(EnchantmentType.SCUBA));
        assertEquals(6, EnchantLevelBoost.WATER_HYACINTH.boosted(Map.of(EnchantmentType.FOREST_PLEDGE, 5)).get(EnchantmentType.FOREST_PLEDGE));
        assertFalse(EnchantLevelBoost.TROUBLED_BUBBLE.appliesTo(Map.of(EnchantmentType.PISCARY, 5)));
        assertFalse(EnchantLevelBoost.VIBRANT_CORAL.appliesTo(Map.of(EnchantmentType.SCUBA, 6)));
    }

    @Test
    void turboGourdRaisesTurboCropFiveToSix() {
        Map<EnchantmentType, Integer> boosted = EnchantLevelBoost.TURBO_GOURD.boosted(
                Map.of(EnchantmentType.TURBO_WHEAT, 5, EnchantmentType.TURBO_COCO, 4));
        assertEquals(6, boosted.get(EnchantmentType.TURBO_WHEAT));
        assertEquals(4, boosted.get(EnchantmentType.TURBO_COCO));
    }

    @Test
    void turboGourdRaisesEveryTurboCropSixToSeven() {
        Map<EnchantmentType, Integer> levels = Map.of(EnchantmentType.TURBO_WHEAT, 6, EnchantmentType.TURBO_CARROT, 6,
                EnchantmentType.TURBO_COCO, 5);
        Map<EnchantmentType, Integer> boosted = EnchantLevelBoost.ENCHANTED_TURBO_GOURD.boosted(levels);
        assertEquals(7, boosted.get(EnchantmentType.TURBO_WHEAT));
        assertEquals(7, boosted.get(EnchantmentType.TURBO_CARROT));
        assertEquals(5, boosted.get(EnchantmentType.TURBO_COCO));
    }
}
