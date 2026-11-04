package dev.daweapon.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.List;

public class EndMiningConfiguration extends SkyBlockRegenConfiguration {
    private static final long REGENERATION_MILLIS = 5000;

    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        task.setIntermediaryBlock(Block.BEDROCK);
        task.setReviveBlock(task.getInitialMinedBlock());
        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return MiningTask.never();
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        return List.of(Material.END_STONE, Material.OBSIDIAN, Material.PURPLE_TERRACOTTA);
    }

    @Override
    public long getRegenerationTime() {
        return REGENERATION_MILLIS;
    }
}
