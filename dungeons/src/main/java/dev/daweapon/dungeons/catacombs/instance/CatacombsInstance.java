package dev.daweapon.dungeons.catacombs.instance;

import dev.daweapon.dungeons.CatacombsDungeon;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.boss.state.BossFightController;
import dev.daweapon.dungeons.catacombs.kit.DungeonClassKit;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public record CatacombsInstance(
        UUID id,
        CatacombsFloorDefinition floor,
        CatacombsDungeon dungeon,
        CatacombsRunState runState,
        BossFightController bossFight,
        Map<UUID, DungeonClassKit> kits,
        Map<Integer, DungeonRoomEncounter> encounters,
        Path renderedMap
) {
    public CatacombsInstance {
        kits = Map.copyOf(kits);
        encounters = Map.copyOf(encounters);
    }
}
