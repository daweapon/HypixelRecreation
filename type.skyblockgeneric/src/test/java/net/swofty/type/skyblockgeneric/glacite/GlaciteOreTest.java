package net.swofty.type.skyblockgeneric.glacite;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.configurations.GlaciteTunnelsConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlaciteOreTest {
    @Test
    void tungstenCobblestoneOnlyCountsInsideTheTunnels() {
        assertEquals(GlaciteOre.TUNGSTEN, GlaciteOre.of(Material.COBBLESTONE, RegionType.GLACITE_TUNNELS));
        assertNull(GlaciteOre.of(Material.COBBLESTONE, RegionType.DWARVEN_MINES));
        assertEquals(MineableBlock.COBBLESTONE, MineableBlock.get(Block.COBBLESTONE, RegionType.DWARVEN_MINES));
        assertEquals(MineableBlock.TUNGSTEN_ORE, MineableBlock.get(Block.COBBLESTONE, RegionType.GLACITE_TUNNELS));
    }

    @Test
    void gemstonesUseTheSameGlassColoursAsHypixel() {
        assertEquals(GlaciteOre.AQUAMARINE, GlaciteOre.of(Material.BLUE_STAINED_GLASS, RegionType.GLACITE_TUNNELS));
        assertEquals(GlaciteOre.CITRINE, GlaciteOre.of(Material.BROWN_STAINED_GLASS, RegionType.GLACITE_TUNNELS));
        assertEquals(GlaciteOre.ONYX, GlaciteOre.of(Material.BLACK_STAINED_GLASS, RegionType.GLACITE_TUNNELS));
        assertEquals(GlaciteOre.PERIDOT, GlaciteOre.of(Material.GREEN_STAINED_GLASS, RegionType.GLACITE_TUNNELS));
        assertNull(GlaciteOre.of(Material.YELLOW_STAINED_GLASS, RegionType.GLACITE_TUNNELS));
    }

    @Test
    void theMapsInfestedCobblestoneIsTungsten() {
        assertEquals(GlaciteOre.TUNGSTEN, GlaciteOre.of(Material.INFESTED_COBBLESTONE, RegionType.GLACITE_TUNNELS));
    }

    @Test
    void everyColdZoneMinesTheSameOres() {
        for (RegionType zone : new RegionType[]{RegionType.GLACITE_TUNNELS, RegionType.GREAT_GLACITE_LAKE, RegionType.GRANDPA_WOLFS_CAVE}) {
            assertEquals(GlaciteOre.GLACITE, GlaciteOre.of(Material.PACKED_ICE, zone), zone.name());
        }
        assertNull(GlaciteOre.of(Material.PACKED_ICE, RegionType.DWARVEN_BASE_CAMP));
        assertNull(GlaciteOre.of(Material.PACKED_ICE, null));
    }

    @Test
    void oreStrengthAndBreakingPowerMatchTheNeuData() {
        assertEquals(5600, MineableBlock.UMBER_ORE.getStrength());
        assertEquals(5600, MineableBlock.TUNGSTEN_ORE.getStrength());
        assertEquals(6000, MineableBlock.GLACITE_ORE.getStrength());
        for (GlaciteOre gem : new GlaciteOre[]{GlaciteOre.AQUAMARINE, GlaciteOre.CITRINE, GlaciteOre.ONYX, GlaciteOre.PERIDOT}) {
            assertEquals(5200, gem.getMineable().getStrength(), gem.name());
        }
        for (GlaciteOre ore : GlaciteOre.values()) assertEquals(9, ore.getMineable().getMiningPowerRequirement(), ore.name());
    }

    @Test
    void oresDropTheirRealItems() {
        assertEquals(ItemType.UMBER, GlaciteOre.UMBER.getDrop());
        assertEquals(ItemType.TUNGSTEN, GlaciteOre.TUNGSTEN.getDrop());
        assertEquals(ItemType.GLACITE, GlaciteOre.GLACITE.getDrop());
        assertEquals(ItemType.ROUGH_PERIDOT_GEM, GlaciteOre.PERIDOT.getDrop());
    }

    @Test
    void metalsPayGlacitePowderAndGemsDoNot() {
        assertTrue(GlaciteOre.UMBER.basePowder() > 0);
        assertTrue(GlaciteOre.GLACITE.basePowder() > 0);
        assertEquals(0, GlaciteOre.ONYX.basePowder());
    }

    @Test
    void tunnelOresRespawnFifteenSecondsAfterMithril() {
        long mithril = RegionType.DWARVEN_MINES.getMiningHandler().getRegenerationTime();
        assertEquals(mithril + 15_000, RegionType.GLACITE_TUNNELS.getMiningHandler().getRegenerationTime());
    }

    @Test
    void glassPanesMineAsTheirGemstone() {
        assertEquals(GlaciteOre.CITRINE, GlaciteOre.of(Material.BROWN_STAINED_GLASS_PANE, RegionType.GLACITE_TUNNELS));
    }

    @Test
    void tunnelRegionsUseTheTunnelRegenerationConfig() {
        assertInstanceOf(GlaciteTunnelsConfiguration.class, RegionType.GLACITE_TUNNELS.getMiningHandler());
        assertTrue(RegionType.GLACITE_TUNNELS.getMiningHandler().getMineableBlocks(null, null).containsAll(GlaciteOre.allMaterials()));
    }

    @Test
    void baseCampResetsColdAndTunnelsAccumulateIt() {
        assertTrue(GlaciteZones.isBaseCamp(RegionType.DWARVEN_BASE_CAMP));
        assertFalse(GlaciteZones.isColdZone(RegionType.DWARVEN_BASE_CAMP));
        assertTrue(GlaciteZones.isColdZone(RegionType.GLACITE_TUNNELS));
        assertTrue(GlaciteZones.isWater(RegionType.GREAT_GLACITE_LAKE));
        assertFalse(GlaciteZones.isWater(RegionType.GLACITE_TUNNELS));
    }
}
