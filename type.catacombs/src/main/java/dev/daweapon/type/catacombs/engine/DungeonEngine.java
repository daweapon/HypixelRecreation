package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.boss.WatcherSpawns;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.type.catacombs.blessing.BlessingStats;
import dev.daweapon.type.catacombs.blessing.PartyBlessings;
import dev.daweapon.type.catacombs.crypt.CryptEngine;
import dev.daweapon.type.catacombs.crypt.CryptPlan;
import dev.daweapon.type.catacombs.map.DungeonMapView;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.type.catacombs.secret.SecretEngine;
import dev.daweapon.type.catacombs.secret.SecretLedger;
import dev.daweapon.type.catacombs.secret.SecretSpot;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Everything that happens inside an active run: rooms, mobs, keys, doors, secrets, crypts, the map, the Blood Room and the boss. */
public final class DungeonEngine {
    private static final int WATCHER_BLESSING_LEVEL = 5;

    private final DungeonHost host;
    private final FloorLayout layout;
    private final CatacombsFloor floor;
    private final KeyDrops keyDrops;
    private final RoomEngine rooms;
    private final DoorEngine doors;
    private final BloodRoomEncounter bloodRoom;
    private final BossEncounter boss;
    private final PartyBlessings blessings;
    private final SecretEngine secrets;
    private final CryptEngine crypts;
    private final DungeonMapView map;
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
        this.blessings = new PartyBlessings(host, BlessingStats.effectiveness(floor));
        this.secrets = new SecretEngine(host, layout, floor, blessings);
        this.crypts = new CryptEngine(host, CryptPlan.of(layout));
        this.map = new DungeonMapView(host, layout, room -> secrets.ledger().allFound(room));
        rooms.discover(layout.startRoom());
        secrets.start();
        crypts.start();
    }

    public void poll() {
        for (SkyBlockPlayer player : host.alivePlayers()) {
            rooms.roomAt(player.getPosition()).ifPresent(this::enter);
        }
        doors.poll();
        keyDrops.poll();
        boss.poll();
        secrets.poll();
        blessings.apply();
        map.poll();
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
        grantWatcherBlessings();
        boss.openPortal();
    }

    /** On every floor after the Entrance, clearing the Blood Room leaves one or two Blessings of Time V and a Blessing of Power V. */
    private void grantWatcherBlessings() {
        int times = 1 + ThreadLocalRandom.current().nextInt(2);
        for (int index = 0; index < times; index++) blessings.grant(BlessingType.TIME, WATCHER_BLESSING_LEVEL, null);
        blessings.grant(BlessingType.POWER, WATCHER_BLESSING_LEVEL, null);
    }

    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        return secrets.interact(player, x, y, z) || crypts.interact(player, x, y, z);
    }

    public Optional<SecretSpot> collectNearestSecret(SkyBlockPlayer player) {
        return secrets.collectNearest(player);
    }

    public boolean blowNearestCrypt(Pos at) {
        return crypts.blowNearest(at);
    }

    public void grantBlessing(BlessingType type, int level, @Nullable SkyBlockPlayer finder) {
        blessings.grant(type, level, finder);
    }

    public SecretLedger secrets() {
        return secrets.ledger();
    }

    public DungeonMapView map() {
        return map;
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
        secrets.dispose();
        crypts.dispose();
        blessings.dispose();
        map.dispose();
    }
}
