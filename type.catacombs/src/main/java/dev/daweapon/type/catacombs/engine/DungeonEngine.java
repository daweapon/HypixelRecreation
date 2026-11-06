package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.type.catacombs.arena.DoorFrame;
import dev.daweapon.type.catacombs.arena.DoorGates;
import dev.daweapon.type.catacombs.engine.watcher.WatcherConfig;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Everything that happens inside an active run: rooms, mobs, keys, doors, secrets, crypts, the map, the Blood Room and the boss. */
public final class DungeonEngine {
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
    private final BloodBlessings bloodBlessings = new BloodBlessings();
    private final FairyRoom fairies;
    private volatile boolean bloodRoomChecked;
    private volatile boolean entranceOpened;

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
        this.fairies = new FairyRoom(host, layout);
        rooms.discover(layout.startRoom());
        secrets.start();
        crypts.start();
        fairies.start();
    }

    /** The Entrance gate sinks away, opening the way out of the Entrance room. */
    public void openEntrance() {
        if (entranceOpened) return;
        entranceOpened = true;
        DoorGates gates = host.arena().blocks().gates();
        for (var door : gates.entranceDoors()) {
            GateLift.run(host.instance(), DoorFrame.passage(door).blocks(), DoorGates.ENTRANCE);
        }
    }

    public void poll() {
        for (SkyBlockPlayer player : host.alivePlayers()) {
            rooms.roomAt(player.getPosition()).ifPresent(this::enter);
        }
        doors.poll();
        keyDrops.poll();
        if (boss.poll()) arrivedAtBoss();
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
        boss.openPortal();
        bloodBlessings.drop(host.instance(), bloodRoom.blessingSpots(), host.arena().roomCentre(layout.bloodRoom()), WatcherConfig.shared());
    }

    private void arrivedAtBoss() {
        bloodRoom.hideBar();
        bloodBlessings.pickUp(host, blessings);
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
        arrivedAtBoss();
    }

    public Pos rewardCentre() {
        return boss.rewardCentre();
    }

    public boolean realBossArena() {
        return boss.realArena();
    }

    /** After the run is over: the exit portal of the boss room. */
    public void pollEnded(List<SkyBlockPlayer> members, Consumer<SkyBlockPlayer> toHub) {
        boss.pollExit(members, toHub);
    }

    public void hideBossBars(SkyBlockPlayer player) {
        bloodRoom.hideBar(player);
        boss.hideBar(player);
    }

    public boolean killBossLife() {
        return boss.killBossLife();
    }

    /** Removes the boss and its arena entities; done when the run's instance closes, not when the run ends. */
    public void disposeBoss() {
        boss.dispose();
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
        bloodRoom.dispose();
        bloodBlessings.clear();
        fairies.dispose();
        rooms.dispose();
        secrets.dispose();
        crypts.dispose();
        blessings.dispose();
        map.dispose();
    }
}
