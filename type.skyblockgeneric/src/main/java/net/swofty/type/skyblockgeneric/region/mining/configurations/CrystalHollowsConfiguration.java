package net.swofty.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsDrops;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsOre;
import net.swofty.type.skyblockgeneric.crystalhollows.HardStone;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CrystalHollowsConfiguration extends SkyBlockRegenConfiguration {
    private static final long REMOVAL_MILLIS = 1000;

    private List<Material> mineable;

    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        task.setIntermediaryBlock(Block.AIR);
        task.setReviveBlock(Block.AIR);
        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return task;
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        if (mineable == null) {
            List<Material> all = new ArrayList<>(CrystalHollowsOre.allMaterials());
            all.addAll(HardStone.materials());
            all.addAll(CrystalHollowsDrops.materials());
            all.addAll(List.of(Material.CYAN_TERRACOTTA, Material.GRAY_WOOL, Material.LIGHT_BLUE_WOOL,
                    Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS, Material.POLISHED_DIORITE,
                    Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE,
                    Material.EMERALD_ORE, Material.DIAMOND_ORE, Material.GOLD_BLOCK, Material.DIRT, Material.NETHERRACK, Material.NETHER_QUARTZ_ORE));
            mineable = List.copyOf(all);
        }
        return mineable;
    }

    @Override
    public long getRegenerationTime() {
        return REMOVAL_MILLIS;
    }
}
