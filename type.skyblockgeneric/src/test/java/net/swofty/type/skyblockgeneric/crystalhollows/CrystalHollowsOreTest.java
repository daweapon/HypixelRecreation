package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalHollowsOreTest {
    @Test
    void everyGemstoneIsMatchedByItsGlassInsideTheHollowsOnly() {
        assertEquals(CrystalHollowsOre.RUBY, CrystalHollowsOre.of(Material.RED_STAINED_GLASS, RegionType.MAGMA_FIELDS));
        assertEquals(CrystalHollowsOre.AMBER, CrystalHollowsOre.of(Material.ORANGE_STAINED_GLASS_PANE, RegionType.GOBLIN_HOLDOUT));
        assertEquals(CrystalHollowsOre.JASPER, CrystalHollowsOre.of(Material.MAGENTA_STAINED_GLASS, RegionType.FAIRY_GROTTO));
        assertNull(CrystalHollowsOre.of(Material.RED_STAINED_GLASS, RegionType.DWARVEN_MINES));
        assertNull(CrystalHollowsOre.of(Material.RED_STAINED_GLASS, null));
    }

    @Test
    void breakingPowerFollowsTheWiki() {
        assertEquals(6, MineableBlock.RUBY_ORE.getMiningPowerRequirement());
        assertEquals(7, MineableBlock.SAPPHIRE_ORE.getMiningPowerRequirement());
        assertEquals(8, MineableBlock.TOPAZ_ORE.getMiningPowerRequirement());
        assertEquals(9, MineableBlock.JASPER_ORE.getMiningPowerRequirement());
        assertEquals(4, MineableBlock.HARD_STONE.getMiningPowerRequirement());
        assertEquals(50, MineableBlock.HARD_STONE.getStrength());
    }

    @Test
    void oresDropTheirRoughGemstone() {
        assertEquals(ItemType.ROUGH_TOPAZ_GEM, CrystalHollowsOre.TOPAZ.getDrop());
        assertEquals(ItemType.ROUGH_RUBY_GEM, CrystalHollowsOre.RUBY.getDrop());
    }

    @Test
    void hardStoneOnlyExistsInsideTheHollows() {
        assertTrue(HardStone.isHardStone(Material.STONE, RegionType.JUNGLE));
        assertTrue(HardStone.isHardStone(Material.ANDESITE, RegionType.CRYSTAL_NUCLEUS));
        assertFalse(HardStone.isHardStone(Material.STONE, RegionType.DWARVEN_MINES));
        assertEquals(MineableBlock.HARD_STONE, MineableBlock.get(net.minestom.server.instance.block.Block.STONE, RegionType.MITHRIL_DEPOSITS));
        assertEquals(MineableBlock.STONE, MineableBlock.get(net.minestom.server.instance.block.Block.STONE, RegionType.DWARVEN_MINES));
    }

    @Test
    void theMinesOfDivanGoldFloorCanBeMined() {
        var config = new net.swofty.type.skyblockgeneric.region.mining.configurations.CrystalHollowsConfiguration();
        assertTrue(config.getMineableBlocks(null, null).contains(Material.GOLD_BLOCK));
        assertTrue(config.getMineableBlocks(null, null).contains(Material.STONE));
    }
}
