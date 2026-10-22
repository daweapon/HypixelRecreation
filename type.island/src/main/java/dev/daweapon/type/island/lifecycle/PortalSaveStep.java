package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.user.island.portal.IslandPortals;

// Island lifecycle step that persists the island's placed portals and removes their holograms on save.
public class PortalSaveStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.SAVE;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        IslandPortals portals = context.island().getPortals();
        if (portals == null) return;
        context.island().getDatabase().insertOrUpdate(IslandPortals.DATABASE_KEY, portals.serialize());
        portals.despawnHolograms();
    }
}
