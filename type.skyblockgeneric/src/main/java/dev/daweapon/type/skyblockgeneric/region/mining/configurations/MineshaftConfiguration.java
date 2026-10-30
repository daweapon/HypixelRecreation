package dev.daweapon.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsOre;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteOre;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Glacite Mineshaft veins stay mined: a broken block turns to air and never grows back. */
public class MineshaftConfiguration extends SkyBlockRegenConfiguration {
    public static final long CLEANUP_MILLIS = 5_000;
    private static final Set<Material> DWARVEN_METALS = Set.of(Material.GRAY_WOOL, Material.CYAN_TERRACOTTA, Material.LIGHT_BLUE_WOOL,
            Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS, Material.POLISHED_DIORITE);

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
            Set<Material> all = new HashSet<>(GlaciteOre.allMaterials());
            all.addAll(CrystalHollowsOre.allMaterials());
            all.addAll(DWARVEN_METALS);
            mineable = List.copyOf(all);
        }
        return mineable;
    }

    @Override
    public long getRegenerationTime() {
        return CLEANUP_MILLIS;
    }
}
