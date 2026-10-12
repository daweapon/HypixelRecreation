package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarTile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarGridLayoutTest {
    @Test
    void tileAreaIsTheSixByFourBlockInsideThePanes() {
        assertEquals(24, BazaarGridLayout.TILE_SLOTS.length);
        assertEquals(11, BazaarGridLayout.TILE_SLOTS[0]);
        assertEquals(16, BazaarGridLayout.TILE_SLOTS[5]);
        assertEquals(20, BazaarGridLayout.TILE_SLOTS[6]);
        assertEquals(43, BazaarGridLayout.TILE_SLOTS[23]);
    }

    @Test
    void panesTabsTilesAndButtonsNeverShareASlot() {
        Set<Integer> seen = new HashSet<>();
        for (int[] group : new int[][]{BazaarGridLayout.TILE_SLOTS, BazaarGridLayout.TAB_SLOTS, BazaarGridLayout.PANE_SLOTS,
                {BazaarGridLayout.SEARCH_SLOT, BazaarGridLayout.SELL_INVENTORY_SLOT, BazaarGridLayout.SELL_SACKS_SLOT,
                        BazaarGridLayout.CLOSE_SLOT, BazaarGridLayout.ORDERS_SLOT, BazaarGridLayout.HISTORY_SLOT,
                        BazaarGridLayout.SETTINGS_SLOT}}) {
            for (int slot : group) {
                assertTrue(seen.add(slot), "slot " + slot + " is used twice");
            }
        }
        assertEquals(54, seen.size());
    }

    @Test
    void buttonRowMatchesTheRealBazaar() {
        assertEquals(45, BazaarGridLayout.SEARCH_SLOT);
        assertEquals(47, BazaarGridLayout.SELL_INVENTORY_SLOT);
        assertEquals(48, BazaarGridLayout.SELL_SACKS_SLOT);
        assertEquals(49, BazaarGridLayout.CLOSE_SLOT);
        assertEquals(50, BazaarGridLayout.ORDERS_SLOT);
        assertEquals(51, BazaarGridLayout.HISTORY_SLOT);
        assertEquals(52, BazaarGridLayout.SETTINGS_SLOT);
        assertEquals(List.of(0, 9, 18, 27, 36), Arrays.stream(BazaarGridLayout.TAB_SLOTS).boxed().toList());
    }

    @Test
    void everyCategoryFitsAndKeepsRealSlots() {
        for (BazaarCategories category : BazaarCategories.values()) {
            List<BazaarTile> tiles = category.tiles();
            int[] slots = BazaarGridLayout.assign(tiles);
            assertEquals(tiles.size(), slots.length);
            Set<Integer> unique = new HashSet<>();
            for (int slot : slots) {
                assertTrue(unique.add(slot), category + " reuses slot " + slot);
            }
            assertFalse(BazaarGridLayout.unusedTileSlots(slots).containsAll(Arrays.stream(slots).boxed().toList()));
        }
    }

    @Test
    void slotsMatchWhereTheVideoShowedTiles() {
        assertEquals(12, slotOf(BazaarCategories.FARMING, "Carrot"));
        assertEquals(24, slotOf(BazaarCategories.FARMING, "Moonflower"));
        assertEquals(25, slotOf(BazaarCategories.FARMING, "Wild Rose"));
        assertEquals(31, slotOf(BazaarCategories.FARMING, "Chicken & Feather"));
        assertEquals(38, slotOf(BazaarCategories.FARMING, "Garden"));

        assertEquals(12, slotOf(BazaarCategories.MINING, "Coal"));
        assertEquals(22, slotOf(BazaarCategories.MINING, "Obsidian"));
        assertEquals(25, slotOf(BazaarCategories.MINING, "Sand"));
        assertEquals(30, slotOf(BazaarCategories.MINING, "Snow"));
        assertEquals(34, slotOf(BazaarCategories.MINING, "Dwarven Mines"));
        assertEquals(38, slotOf(BazaarCategories.MINING, "Forge"));
        assertEquals(40, slotOf(BazaarCategories.MINING, "Crimson Isle"));
        assertEquals(41, slotOf(BazaarCategories.MINING, "Glacite Tunnels"));
        assertEquals(42, slotOf(BazaarCategories.MINING, "Fossils"));

        assertEquals(11, slotOf(BazaarCategories.COMBAT, "Rotten Flesh"));
        assertEquals(15, slotOf(BazaarCategories.COMBAT, "Ender Pearl"));
        assertEquals(21, slotOf(BazaarCategories.COMBAT, "Magma Cream"));
        assertEquals(23, slotOf(BazaarCategories.COMBAT, "The End"));
        assertEquals(25, slotOf(BazaarCategories.COMBAT, "Slayers"));
        assertEquals(29, slotOf(BazaarCategories.COMBAT, "Catacombs"));

        assertEquals(11, slotOf(BazaarCategories.WOODS_AND_FISHES, "Wood"));
        assertEquals(15, slotOf(BazaarCategories.WOODS_AND_FISHES, "Clay"));
        assertEquals(21, slotOf(BazaarCategories.WOODS_AND_FISHES, "Ink Sac"));
        assertEquals(22, slotOf(BazaarCategories.WOODS_AND_FISHES, "Baits"));
        assertEquals(23, slotOf(BazaarCategories.WOODS_AND_FISHES, "Mob Drops"));
        assertEquals(25, slotOf(BazaarCategories.WOODS_AND_FISHES, "Lava Fishing"));
        assertEquals(29, slotOf(BazaarCategories.WOODS_AND_FISHES, "Water Orbs"));
        assertEquals(31, slotOf(BazaarCategories.WOODS_AND_FISHES, "Backwater Bayou"));
        assertEquals(33, slotOf(BazaarCategories.WOODS_AND_FISHES, "Torrhus Canyon"));
        assertEquals(34, slotOf(BazaarCategories.WOODS_AND_FISHES, "Lotus Atoll"));

        assertEquals(11, slotOf(BazaarCategories.ODDITIES, "Booster Cookie"));
        assertEquals(12, slotOf(BazaarCategories.ODDITIES, "Modifiers"));
        assertEquals(13, slotOf(BazaarCategories.ODDITIES, "Enchantments"));
        assertEquals(14, slotOf(BazaarCategories.ODDITIES, "EXP Bottles"));
        assertEquals(15, slotOf(BazaarCategories.ODDITIES, "Shards"));
        assertEquals(16, slotOf(BazaarCategories.ODDITIES, "Reforge Stones"));
        assertEquals(20, slotOf(BazaarCategories.ODDITIES, "Power Stones"));
        assertEquals(21, slotOf(BazaarCategories.ODDITIES, "Stock of Stonks"));
        assertEquals(22, slotOf(BazaarCategories.ODDITIES, "Pet Upgrades"));
        assertEquals(23, slotOf(BazaarCategories.ODDITIES, "Inferno Minion"));
        assertEquals(24, slotOf(BazaarCategories.ODDITIES, "Minion Upgrades"));
        assertEquals(25, slotOf(BazaarCategories.ODDITIES, "Fuels"));
        assertEquals(29, slotOf(BazaarCategories.ODDITIES, "Essence"));
        assertEquals(31, slotOf(BazaarCategories.ODDITIES, "Consumables"));
        assertEquals(32, slotOf(BazaarCategories.ODDITIES, "Cross-Dimensional"));
        assertEquals(33, slotOf(BazaarCategories.ODDITIES, "Upgrade Fragments"));
    }

    private static int slotOf(BazaarCategories category, String name) {
        List<BazaarTile> tiles = category.tiles();
        int[] slots = BazaarGridLayout.assign(tiles);
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).name().equals(name)) {
                return slots[i];
            }
        }
        throw new AssertionError(name + " is not a tile of " + category);
    }
}
