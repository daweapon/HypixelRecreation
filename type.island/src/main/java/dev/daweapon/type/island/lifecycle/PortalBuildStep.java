package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.structure.structures.IslandPortal;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;

// Island lifecycle step that builds the Hub travel portal when the Island loads.
public class PortalBuildStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        IslandPortal portal = new IslandPortal(0, 6, 100, 43);
        portal.setType(IslandPortal.PortalType.HUB);
        portal.build(context.island().getIslandInstance());
    }
}
