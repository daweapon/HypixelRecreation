package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Entering a room spawns its mobs; killing the last starred one clears it, which may drop the room's key. */
final class RoomEngine implements CatacombsMob.Listener {
    private static final int NO_ROOM = -1;

    private final DungeonHost host;
    private final FloorLayout layout;
    private final KeyDrops keyDrops;
    private final KeyDropPlan keyPlan;
    private final DungeonMobRegistry registry = DungeonMobRegistry.defaults();
    private final RoomTracker tracker = new RoomTracker();
    private final Set<CatacombsMob> alive = ConcurrentHashMap.newKeySet();

    RoomEngine(DungeonHost host, FloorLayout layout, KeyDrops keyDrops) {
        this.host = host;
        this.layout = layout;
        this.keyDrops = keyDrops;
        this.keyPlan = KeyDropPlan.of(layout);
    }

    Optional<PlacedRoom> roomAt(Pos position) {
        return CellGeometry.cellAt(position.blockX(), position.blockZ()).flatMap(layout::roomAt);
    }

    void discover(PlacedRoom room) {
        if (!tracker.discover(room.index())) return;
        int exploredBefore = tracker.discoveredCount() - 1;
        host.state().markRoom(room.index(), DungeonRoomState.DISCOVERED);
        if (room.index() != layout.startRoom().index()) host.advanceTo(DungeonRunPhase.CLEARING);
        int starred = spawnMobs(room, exploredBefore);
        if (starred == 0 && completesOnEntry(room)) complete(room);
    }

    private static boolean completesOnEntry(PlacedRoom room) {
        return room.kind() != RoomKind.PUZZLE && room.kind() != RoomKind.BLOOD;
    }

    private int spawnMobs(PlacedRoom room, int exploredBefore) {
        int starred = 0;
        for (MobSpawnPoint spawn : room.template().mobSpawns()) {
            DungeonMobDefinition definition = definition(spawn.mobId());
            if (definition == null) continue;
            CatacombsMob mob = new CatacombsMob(definition.scaleForRoomsExplored(exploredBefore), room.index(), spawn.starred(), this);
            alive.add(mob);
            mob.setInstance(host.instance(), host.arena().mobSpawn(room, spawn));
            if (spawn.starred()) starred++;
        }
        if (starred > 0) tracker.registerStarred(room.index(), starred);
        return starred;
    }

    private @Nullable DungeonMobDefinition definition(String mobId) {
        try {
            return registry.mob(mobId);
        } catch (IllegalArgumentException exception) {
            Logger.warn("Room template spawns unknown dungeon mob {}", mobId);
            return null;
        }
    }

    @Override
    public void mobDied(CatacombsMob mob) {
        alive.remove(mob);
        if (mob.starred() && tracker.starredKilled(mob.room())) {
            PlacedRoom room = layout.room(mob.room());
            host.later(1, () -> complete(room));
        }
    }

    private void complete(PlacedRoom room) {
        host.state().markRoom(room.index(), DungeonRoomState.COMPLETED);
        keyPlan.keyFor(room.index()).ifPresent(key -> keyDrops.drop(key, host.arena().roomCentre(room)));
    }

    boolean clear(PlacedRoom room, SkyBlockPlayer actor) {
        discover(room);
        for (CatacombsMob mob : List.copyOf(alive)) {
            if (mob.room() == room.index()) mob.obliterate(actor);
        }
        if (!tracker.forceClear(room.index())) return false;
        complete(room);
        return true;
    }

    CatacombsMob spawnLoose(String mobId, Pos at) {
        DungeonMobDefinition definition = definition(mobId);
        if (definition == null) return null;
        CatacombsMob mob = new CatacombsMob(definition, NO_ROOM, false, this);
        alive.add(mob);
        mob.setInstance(host.instance(), at);
        return mob;
    }

    int discoveredRooms() {
        return tracker.discoveredCount();
    }

    boolean isCleared(int room) {
        return tracker.isCleared(room);
    }

    void dispose() {
        alive.forEach(CatacombsMob::remove);
        alive.clear();
    }
}
