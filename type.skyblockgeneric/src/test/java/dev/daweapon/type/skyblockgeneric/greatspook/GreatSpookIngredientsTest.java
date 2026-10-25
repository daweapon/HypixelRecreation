package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreatSpookIngredientsTest {
    @Test
    void anyZombieKindCountsButOtherMobsDoNot() {
        assertTrue(GreatSpookIngredients.ZOMBIES.contains(EntityType.ZOMBIE));
        assertTrue(GreatSpookIngredients.ZOMBIES.contains(EntityType.ZOMBIE_VILLAGER));
        assertFalse(GreatSpookIngredients.ZOMBIES.contains(EntityType.SKELETON));
        assertFalse(GreatSpookIngredients.ZOMBIES.contains(EntityType.SPIDER));
    }

    @Test
    void sweetFleshAndDeadSeedAreGuaranteedAndFrozenSpiderIsRare() {
        assertEquals(100, GreatSpookIngredients.SWEET_FLESH_CHANCE);
        assertEquals(100, GreatSpookIngredients.DEAD_SEED_CHANCE);
        assertTrue(GreatSpookIngredients.FROZEN_SPIDER_CHANCE < 100);
    }

    @Test
    void ingredientToolsUseTheCatalogueIds() {
        assertEquals("SWEET_AXE", GreatSpookIngredients.SWEET_AXE_ID);
        assertEquals("DECORATION_APPALLED_PUMPKIN", GreatSpookIngredients.APPALLED_PUMPKIN_ID);
        assertEquals("ICY_SINKER", GreatSpookCatches.ICY_SINKER_ID);
        assertEquals("SPOOKY_BAIT", GreatSpookCatches.SPOOKY_BAIT_ID);
    }
}
