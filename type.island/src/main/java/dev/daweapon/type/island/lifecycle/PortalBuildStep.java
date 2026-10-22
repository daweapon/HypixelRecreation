package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.user.island.portal.IslandPortals;

// Island lifecycle step that restores the island's placed portals and their holograms on load.
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
        var database = context.island().getDatabase();
        String raw = database.has(IslandPortals.DATABASE_KEY) ? (String) database.get(IslandPortals.DATABASE_KEY, String.class) : null;
        IslandPortals portals = IslandPortals.deserialize(raw);
        context.island().setPortals(portals);
        portals.spawnAll(context.island().getIslandInstance());
    }
}
