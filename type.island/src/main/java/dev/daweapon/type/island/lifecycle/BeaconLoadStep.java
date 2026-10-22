package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.beacon.BeaconData;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconEffectTicker;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconService;
import dev.daweapon.type.skyblockgeneric.block.BlockType;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that restores the placed Beacon block and starts its island effect ticker when the Island loads.
public class BeaconLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 31;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        BeaconData data = BeaconService.get(context.island());
        if (data == null) return;
        var instance = context.island().getIslandInstance();
        instance.setBlock(data.x(), data.y(), data.z(), new SkyBlockBlock(BlockType.BEACON).toBlock());
        BeaconEffectTicker.ensureRunning(context.island());
    }
}
