package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.type.catacombs.blessing.PartyBlessings;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** What a puzzle room needs from its run: the host, the floor, the party's blessings and the room's outcome hooks. */
public record PuzzleContext(DungeonHost host, CatacombsFloor floor, PartyBlessings blessings, Consumer<PlacedRoom> completed,
                            BiConsumer<PlacedRoom, @Nullable SkyBlockPlayer> failed) {
}
