package dev.daweapon.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.List;

public class MineWheatConfiguration extends SkyBlockRegenConfiguration {
    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        task.setIntermediaryBlock(Block.AIR);
        // Revive must already be the mature crop: the regen repeater applies reviveBlock
        // directly with no further growth ticking, so a default (age=0) block would leave
        // farmland permanently bare instead of regrowing wheat.
        task.setReviveBlock(Block.WHEAT.withProperty("age", "7"));

        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return MiningTask.never();
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        return List.of(Material.WHEAT);
    }

    @Override
    public long getRegenerationTime() {
        return 1;
    }
}
