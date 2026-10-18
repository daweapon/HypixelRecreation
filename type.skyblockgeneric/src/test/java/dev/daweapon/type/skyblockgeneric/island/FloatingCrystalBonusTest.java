package dev.daweapon.type.skyblockgeneric.island;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloatingCrystalBonusTest {
    @Test
    void farmCrystalBoostsOnlyFarmingMinions() {
        Set<ItemType> placed = Set.of(ItemType.FARM_CRYSTAL);
        assertEquals(10, FloatingCrystalBonus.percent(placed, "WHEAT", 3), 1e-9);
        assertEquals(0, FloatingCrystalBonus.percent(placed, "OAK", 3), 1e-9);
    }

    @Test
    void cornucopiaAddsOnePercentPerUniqueFarmingMinion() {
        Set<ItemType> placed = Set.of(ItemType.CORNUCOPIA_CRYSTAL);
        assertEquals(10, FloatingCrystalBonus.percent(placed, "CARROT", 0), 1e-9);
        assertEquals(17, FloatingCrystalBonus.percent(placed, "CARROT", 7), 1e-9);
    }

    @Test
    void farmAndCornucopiaStack() {
        Set<ItemType> placed = Set.of(ItemType.FARM_CRYSTAL, ItemType.CORNUCOPIA_CRYSTAL);
        assertEquals(22, FloatingCrystalBonus.percent(placed, "PIG", 2), 1e-9);
    }

    @Test
    void woodcuttingAndWinterCrystalsTargetTheirCategories() {
        assertEquals(10, FloatingCrystalBonus.percent(Set.of(ItemType.WOODCUTTING_CRYSTAL), "BIRCH", 0), 1e-9);
        assertEquals(5, FloatingCrystalBonus.percent(Set.of(ItemType.WINTER_ISLAND_CRYSTAL), "SNOW", 0), 1e-9);
        assertEquals(5, FloatingCrystalBonus.percent(Set.of(ItemType.WINTER_ISLAND_CRYSTAL), "ICE", 0), 1e-9);
        assertEquals(0, FloatingCrystalBonus.percent(Set.of(ItemType.WINTER_ISLAND_CRYSTAL), "COAL", 0), 1e-9);
    }

    @Test
    void postcardBoostsEveryMinion() {
        assertEquals(5, FloatingCrystalBonus.percent(Set.of(ItemType.POSTCARD), "COBBLESTONE", 0), 1e-9);
    }
}
