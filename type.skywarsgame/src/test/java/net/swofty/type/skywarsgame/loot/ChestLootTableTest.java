package net.swofty.type.skywarsgame.loot;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChestLootTableTest {
    private static final int ROLLS = 500;

    @Test
    void islandChestHoldsThreeToFiveItems() {
        for (int i = 0; i < ROLLS; i++) {
            int size = ChestLootTable.generateLoot(LootTier.ISLAND, false).size();
            assertTrue(size >= 3 && size <= 5, "size " + size);
        }
    }

    @Test
    void centerChestHoldsFiveToEightItems() {
        for (int i = 0; i < ROLLS; i++) {
            int size = ChestLootTable.generateLoot(LootTier.CENTER, true).size();
            assertTrue(size >= 5 && size <= 8, "size " + size);
        }
    }

    @Test
    void normalIslandLootNeverContainsDiamondOrNetherite() {
        for (int i = 0; i < ROLLS; i++) {
            for (ItemStack item : ChestLootTable.generateLoot(LootTier.ISLAND, false)) {
                String name = item.material().name();
                assertFalse(name.contains("diamond") || name.contains("netherite"), name);
                assertFalse(item.isAir());
            }
        }
    }

    @Test
    void islandLootNeverContainsTotemOrEnchantedApple() {
        for (int i = 0; i < ROLLS; i++) {
            for (ItemStack item : ChestLootTable.generateLoot(LootTier.ISLAND, true)) {
                assertNotEquals(Material.TOTEM_OF_UNDYING, item.material());
                assertNotEquals(Material.ENCHANTED_GOLDEN_APPLE, item.material());
            }
        }
    }

    @Test
    void stackAmountsStayWithinVanillaLimits() {
        for (int i = 0; i < ROLLS; i++) {
            List<ItemStack> loot = ChestLootTable.generateLoot(LootTier.CENTER, false);
            for (ItemStack item : loot) {
                assertTrue(item.amount() >= 1 && item.amount() <= 64);
            }
        }
    }
}
