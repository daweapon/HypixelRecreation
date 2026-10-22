package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.block.BlockType;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPads;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that restores the island's Teleport Pad blocks when it loads.
public class TeleportPadLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 35;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        var instance = context.island().getIslandInstance();
        TeleportPads.of(context.island()).all().forEach(pad ->
                instance.setBlock(pad.x(), pad.y(), pad.z(), new SkyBlockBlock(BlockType.TELEPORT_PAD).toBlock()));
    }
}
