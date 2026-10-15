package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.boss.WatcherSpawns;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** The placeholder Watcher: spawns the floor's quota of blood mobs, and once they are all dead the room is done. */
final class BloodRoomEncounter implements CatacombsMob.Listener {
    private static final int SPAWN_GAP_TICKS = 30;
    private static final double RING_RADIUS = 9;
    private static final String MOB_ID = "WATCHER_UNDEAD";

    private final DungeonHost host;
    private final PlacedRoom room;
    private final CatacombsFloor floor;
    private final Runnable onCleared;
    private final AtomicBoolean started = new AtomicBoolean();
    private final List<CatacombsMob> spawned = new ArrayList<>();
    private int killed;
    private boolean finished;

    BloodRoomEncounter(DungeonHost host, PlacedRoom room, CatacombsFloor floor, Runnable onCleared) {
        this.host = host;
        this.room = room;
        this.floor = floor;
        this.onCleared = onCleared;
    }

    boolean start() {
        if (!started.compareAndSet(false, true)) return false;
        host.advanceTo(DungeonRunPhase.BLOOD_ROOM);
        host.state().markRoom(room.index(), DungeonRoomState.DISCOVERED);
        host.broadcast(CatacombsMessages.watcherIntro());
        DungeonMobDefinition definition = DungeonMobRegistry.defaults().mob(MOB_ID);
        int count = WatcherSpawns.mobCount(floor);
        Pos centre = host.arena().roomCentre(room);
        for (int index = 0; index < count; index++) {
            double angle = Math.TAU * index / count;
            Pos at = new Pos(centre.x() + Math.cos(angle) * RING_RADIUS, centre.y(), centre.z() - 4 + Math.sin(angle) * RING_RADIUS);
            host.later(SPAWN_GAP_TICKS * (index + 1), () -> spawn(definition, at));
        }
        return true;
    }

    private synchronized void spawn(DungeonMobDefinition definition, Pos at) {
        if (finished) return;
        CatacombsMob mob = new CatacombsMob(definition, room.index(), false, this);
        spawned.add(mob);
        mob.setInstance(host.instance(), at);
    }

    @Override
    public synchronized void mobDied(CatacombsMob mob) {
        if (finished || !WatcherSpawns.cleared(floor, ++killed)) return;
        complete();
    }

    synchronized void finish() {
        if (finished) return;
        complete();
    }

    private void complete() {
        finished = true;
        spawned.forEach(CatacombsMob::remove);
        host.state().markRoom(room.index(), DungeonRoomState.COMPLETED);
        host.later(1, onCleared);
    }

    boolean started() {
        return started.get();
    }
}
