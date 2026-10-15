package net.swofty.type.island.lifecycle;

import net.minestom.server.coordinate.Point;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.block.BlockType;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.experiments.ExperimentationTableDisplay;
import net.swofty.type.skyblockgeneric.island.IslandUtilityBlocks;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleContext;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import net.swofty.type.skyblockgeneric.user.island.IslandLifecycleStep;

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
