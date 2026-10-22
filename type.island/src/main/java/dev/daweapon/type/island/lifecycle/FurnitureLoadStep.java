package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.IslandFurniture;

// Island lifecycle step that restores the island's placed furniture and its bed spawn point on load.
public class FurnitureLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 25;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        var database = context.island().getDatabase();
        String raw = database.has(IslandFurniture.DATABASE_KEY) ? (String) database.get(IslandFurniture.DATABASE_KEY, String.class) : null;
        String spawn = database.has(IslandFurniture.SPAWN_KEY) ? (String) database.get(IslandFurniture.SPAWN_KEY, String.class) : null;
        IslandFurniture furniture = IslandFurniture.deserialize(raw, spawn);
        context.island().setFurniture(furniture);
        furniture.spawnAll(context.island().getIslandInstance());
        if (furniture.spawnPoint() != null) {
            context.onlineMembers().forEach(member -> member.setRespawnPoint(furniture.spawnPoint()));
        }
    }
}
