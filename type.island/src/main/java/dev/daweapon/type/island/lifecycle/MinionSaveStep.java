package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that removes minion entities and persists minion data on save.
public class MinionSaveStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.SAVE;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        IslandMinionData minionData = context.island().getMinionData();
        minionData.getMinions().forEach(IslandMinionData.IslandMinion::removeMinion);
        context.island().getDatabase().insertOrUpdate("minions", minionData.serialize());
    }
}
