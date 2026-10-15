package dev.daweapon.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteOre;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.List;

/**
 * Glacite Tunnels ores (Umber, Tungsten, Glacite and the four tunnel gemstones) grow back in place
 * through bedrock. The wiki gives no regeneration time, so it is the Mithril time plus 15 seconds (user decision).
 */
public class GlaciteTunnelsConfiguration extends SkyBlockRegenConfiguration {
    public static final long EXTRA_REGENERATION_MILLIS = 15_000;

    private List<Material> mineable;

    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        task.setIntermediaryBlock(Block.BEDROCK);
        task.setReviveBlock(task.getInitialMinedBlock());
        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return task;
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        if (mineable == null) mineable = List.copyOf(GlaciteOre.allMaterials());
        return mineable;
    }

    @Override
    public long getRegenerationTime() {
        return DwarvenMinesConfiguration.REGENERATION_MILLIS + EXTRA_REGENERATION_MILLIS;
    }
}
