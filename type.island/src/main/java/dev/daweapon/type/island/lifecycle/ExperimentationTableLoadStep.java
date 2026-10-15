package dev.daweapon.type.island.lifecycle;

import net.minestom.server.coordinate.Point;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.block.BlockType;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentationTableDisplay;
import dev.daweapon.type.skyblockgeneric.island.IslandUtilityBlocks;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that restores the Experimentation Table block and its model display when the Island loads.
public class ExperimentationTableLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        var instance = context.island().getIslandInstance();
        for (Point position : IslandUtilityBlocks.positions(instance, ItemType.EXPERIMENTATION_TABLE)) {
            instance.setBlock(position, new SkyBlockBlock(BlockType.EXPERIMENTATION_TABLE).toBlock());
            ExperimentationTableDisplay.spawn(instance, position);
        }
    }
}
