package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ItemTypeResolver;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.region.mining.configurations.CrystalHollowsConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalHollowsMiningTest {
    @Test
    void gemstonePanesResolveToTheSameMineableAsTheirBlocks() {
        for (CrystalHollowsOre ore : CrystalHollowsOre.values()) {
            for (Material material : ore.getMaterials()) {
                assertEquals(ore.getMineable(), MineableBlock.get(material.block(), RegionType.JUNGLE), material.name());
            }
        }
        assertNull(MineableBlock.get(Material.LIME_STAINED_GLASS_PANE.block(), RegionType.DWARVEN_MINES));
    }

    @Test
    void everyGemstoneMaterialAndDivanBlockIsMineableInTheHollows() {
        List<Material> mineable = new CrystalHollowsConfiguration().getMineableBlocks(null, null);
        assertTrue(mineable.containsAll(CrystalHollowsOre.allMaterials()));
        assertTrue(mineable.containsAll(CrystalHollowsDrops.materials()));
    }

    @Test
    void divanBlocksHaveAMineableDefinition() {
        for (Material material : CrystalHollowsDrops.materials()) {
            assertTrue(MineableBlock.get(material.block(), RegionType.MINES_OF_DIVAN) != null, material.name());
        }
    }

    @Test
    void mithrilAndTitaniumBlocksDropTheirItemsInsteadOfTheBlock() {
        assertEquals(ItemType.MITHRIL, CrystalHollowsDrops.of(Material.PRISMARINE, RegionType.MITHRIL_DEPOSITS).item());
        assertEquals(ItemType.MITHRIL, CrystalHollowsDrops.of(Material.LIGHT_BLUE_WOOL, RegionType.JUNGLE).item());
        assertEquals(ItemType.MITHRIL, CrystalHollowsDrops.of(Material.DARK_PRISMARINE, RegionType.JUNGLE).item());
        assertEquals(ItemType.TITANIUM, CrystalHollowsDrops.of(Material.POLISHED_DIORITE, RegionType.JUNGLE).item());
        assertNull(CrystalHollowsDrops.of(Material.PRISMARINE, RegionType.DWARVEN_MINES));
    }

    @Test
    void divanOresDropTheirNormalItems() {
        assertEquals(ItemType.GOLD_INGOT, CrystalHollowsDrops.of(Material.GOLD_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.EMERALD, CrystalHollowsDrops.of(Material.EMERALD_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.DIAMOND, CrystalHollowsDrops.of(Material.DIAMOND_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.LAPIS_LAZULI, CrystalHollowsDrops.of(Material.LAPIS_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.REDSTONE, CrystalHollowsDrops.of(Material.REDSTONE_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.IRON_INGOT, CrystalHollowsDrops.of(Material.IRON_BLOCK, RegionType.MINES_OF_DIVAN).item());
        assertEquals(ItemType.COAL, CrystalHollowsDrops.of(Material.COAL_BLOCK, RegionType.MINES_OF_DIVAN).item());
    }

    @Test
    void dropCountsStayInsideTheirRange() {
        CrystalHollowsDrops.Drop gold = CrystalHollowsDrops.of(Material.GOLD_BLOCK, RegionType.MINES_OF_DIVAN);
        Random random = new Random(3);
        for (int i = 0; i < 200; i++) {
            int amount = gold.roll(random);
            assertTrue(amount >= gold.min() && amount <= gold.max());
        }
    }

    @Test
    void miningXpIsGrantedForGemstonesMithrilAndOres() {
        assertEquals(OptionalDouble.of(CrystalHollowsDrops.GEMSTONE_MINING_XP), CrystalHollowsDrops.miningXp(Material.RED_STAINED_GLASS_PANE, RegionType.MAGMA_FIELDS));
        assertEquals(OptionalDouble.of(6), CrystalHollowsDrops.miningXp(Material.GRAY_WOOL, RegionType.JUNGLE));
        assertEquals(OptionalDouble.of(6), CrystalHollowsDrops.miningXp(Material.GOLD_BLOCK, RegionType.MINES_OF_DIVAN));
        assertEquals(OptionalDouble.empty(), CrystalHollowsDrops.miningXp(Material.GOLD_BLOCK, RegionType.DWARVEN_MINES));
    }

    @Test
    void metalDetectorIsGivableUnderItsShortName() {
        assertEquals(ItemType.DWARVEN_METAL_DETECTOR, ItemTypeResolver.resolve("metal_detector"));
        assertEquals(ItemType.DWARVEN_METAL_DETECTOR, ItemTypeResolver.resolve("dwarven_metal_detector"));
    }

    @Test
    void metalDetectorDistanceLabelShowsOneDecimal() {
        assertEquals("12.3 meters away", MetalDetector.distanceLabel(12.34));
        assertEquals("0.0 meters away", MetalDetector.distanceLabel(0));
        assertTrue(MetalDetector.beepPitch(2) > MetalDetector.beepPitch(25));
        assertTrue(MetalDetector.beepIntervalTicks(2) < MetalDetector.beepIntervalTicks(25));
    }
}
