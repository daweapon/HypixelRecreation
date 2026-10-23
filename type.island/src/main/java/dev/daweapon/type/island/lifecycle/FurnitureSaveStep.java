package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.IslandFurniture;

// Island lifecycle step that persists the island's placed furniture and removes its entities on save.
public class FurnitureSaveStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.SAVE;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        IslandFurniture furniture = context.island().getFurniture();
        if (furniture == null) return;
        var database = context.island().getDatabase();
        database.insertOrUpdate(IslandFurniture.DATABASE_KEY, furniture.serialize());
        database.insertOrUpdate(IslandFurniture.STORAGE_KEY, furniture.serializeStorage());
        String spawn = furniture.serializeSpawn();
        database.insertOrUpdate(IslandFurniture.SPAWN_KEY, spawn == null ? "" : spawn);
        furniture.despawnAll();
    }
}
