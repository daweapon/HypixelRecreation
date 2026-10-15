package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoblinEggOreSpawnTest {
    @Test
    void hollowsGemstonesAndMithrilGiveATenthOfAPercent() {
        assertEquals(0.1, GoblinEggs.oreSpawnChance(Material.RED_STAINED_GLASS, RegionType.MAGMA_FIELDS));
        assertEquals(0.1, GoblinEggs.oreSpawnChance(Material.PRISMARINE, RegionType.MITHRIL_DEPOSITS));
    }

    @Test
    void hardStoneGivesAHundredthOfAPercent() {
        assertEquals(0.01, GoblinEggs.oreSpawnChance(Material.STONE, RegionType.JUNGLE));
    }

    @Test
    void customOresOutsideTheHollowsGiveATenthOfAPercent() {
        assertEquals(0.1, GoblinEggs.oreSpawnChance(Material.GOLD_ORE, RegionType.DWARVEN_MINES));
        assertEquals(0, GoblinEggs.oreSpawnChance(Material.GOLD_ORE, RegionType.MAGMA_FIELDS));
        assertEquals(0, GoblinEggs.oreSpawnChance(Material.STONE, RegionType.DWARVEN_MINES));
    }
}
