package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.boss.WatcherSpawns;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Everything that happens inside an active run: rooms, mobs, keys, doors, the Blood Room and the boss. */
public final class DungeonEngine {
    private final DungeonHost host;
    private final FloorLayout layout;
    private final CatacombsFloor floor;
    private final KeyDrops keyDrops;
    private final RoomEngine rooms;
    private final DoorEngine doors;
    private final BloodRoomEncounter bloodRoom;
    private final BossEncounter boss;
    private volatile boolean bloodRoomChecked;

    public DungeonEngine(DungeonHost host, FloorLayout layout, CatacombsFloorDefinition definition) {
        this.host = host;
        this.layout = layout;
        this.floor = definition.floor();
        this.keyDrops = new KeyDrops(host);
        this.rooms = new RoomEngine(host, layout, keyDrops);
        this.doors = new DoorEngine(host, layout.doors());
        PlacedRoom blood = layout.bloodRoom();
        this.boss = new BossEncounter(host, floor, blood);
        this.bloodRoom = new BloodRoomEncounter(host, blood, floor, this::bloodRoomCleared);
        rooms.discover(layout.startRoom());
    }

    public void poll() {
        for (SkyBlockPlayer player : host.alivePlayers()) {
            rooms.roomAt(player.getPosition()).ifPresent(this::enter);
        }
        doors.poll();
        keyDrops.poll();
        boss.poll();
    }

    private void enter(PlacedRoom room) {
        if (room.kind() != RoomKind.BLOOD) {
            rooms.discover(room);
            return;
        }
        if (bloodRoomChecked) return;
        bloodRoomChecked = true;
        if (BloodRoomGate.onEnter(doors.bloodDoorOpened()) == BloodRoomGate.Verdict.FAILS_RUN) {
            host.lose(CatacombsMessages.bloodRoomWithoutKey());
            return;
        }
        bloodRoom.start();
    }

    private void bloodRoomCleared() {
        if (WatcherSpawns.completesRunWhenCleared(floor)) {
            host.broadcast(CatacombsMessages.watcherEntranceDone());
            host.win();
            return;
        }
        host.broadcast(CatacombsMessages.watcherDone());
        boss.openPortal();
    }

    public Optional<PlacedRoom> roomAt(Pos position) {
        return rooms.roomAt(position);
    }

    public boolean clearRoom(PlacedRoom room, SkyBlockPlayer actor) {
        if (room.kind() == RoomKind.BLOOD) {
            bloodRoom.finish();
            return true;
        }
        return rooms.clear(room, actor);
    }

    public void clearAll(SkyBlockPlayer actor) {
        for (PlacedRoom room : layout.rooms()) {
            if (room.kind() != RoomKind.BLOOD) rooms.clear(room, actor);
        }
    }

    public void giveKey(KeyType key) {
        host.keys().grant(key);
        host.broadcast("<a>The party was given a " + key.coloured() + "<a>.");
    }

    public boolean openNextDoor(@Nullable SkyBlockPlayer actor) {
        Optional<LayoutDoor> next = doors.nextClosed(layout.criticalPath());
        next.ifPresent(door -> doors.open(door, actor, false));
        return next.isPresent();
    }

    public @Nullable CatacombsMob spawnMob(String mobId, Pos at) {
        return rooms.spawnLoose(mobId, at);
    }

    public void enterBoss() {
        boss.openPortal();
        boss.enter();
    }

    public int discoveredRooms() {
        return rooms.discoveredRooms();
    }

    public long openedDoors() {
        return doors.openedCount();
    }

    public boolean bloodDoorOpened() {
        return doors.bloodDoorOpened();
    }

    public void dispose() {
        keyDrops.clear();
        rooms.dispose();
    }
}
