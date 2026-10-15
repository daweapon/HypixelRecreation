package dev.daweapon.dungeons.catacombs.generation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.daweapon.dungeons.DungeonRoomType;
import dev.daweapon.dungeons.DungeonsAPI;
import dev.daweapon.dungeons.DungeonsData;
import dev.daweapon.dungeons.GeneratorService;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Builds a GeneratorService configured for a specific CatacombsFloorDefinition's size and room counts.
public final class CatacombsGenerator {
    public static GeneratorService generator(CatacombsFloorDefinition definition) {
        DungeonsData data = new DungeonsData(
                definition.floor().dungeonSize().width(),
                definition.floor().dungeonSize().height())
                .with(DungeonRoomType.FAIRY, new DungeonsData.RoomData(1, 1))
                .with(DungeonRoomType.PUZZLE, new DungeonsData.RoomData(definition.puzzleRooms(), definition.puzzleRooms()))
                .with(DungeonRoomType.MINI_BOSS, new DungeonsData.RoomData(definition.minibossRooms(), definition.minibossRooms()));

        if (definition.trapRooms() > 0) {
            data.with(DungeonRoomType.TRAP, new DungeonsData.RoomData(definition.trapRooms(), definition.trapRooms()));
        }

        return DungeonsAPI.getGeneratorService(data);
    }
}
