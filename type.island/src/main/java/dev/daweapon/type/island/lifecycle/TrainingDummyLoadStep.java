package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy.TrainingDummies;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that respawns the island's Training Dummies when it loads.
public class TrainingDummyLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        TrainingDummies.spawnAll(context.island());
    }
}
