package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.boss.WatcherSpawns;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.engine.look.MobLooks;
import dev.daweapon.type.catacombs.engine.watcher.WatcherConfig;
import dev.daweapon.type.catacombs.engine.watcher.WatcherEntity;
import dev.daweapon.type.catacombs.engine.watcher.WatcherWaves;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.commons.text.Text;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Blood Room as captured on Floor I: the Watcher floats in, talks, and summons the floor's undead one by one at
 * the middle of the room following {@link WatcherWaves}; the boss bar counts the summons left to kill; once all are
 * dead and he has said his piece, the room is done and the portal opens.
 */
final class BloodRoomEncounter implements CatacombsMob.Listener {
    private static final String SUMMON_ID = "WATCHER_UNDEAD";
    private static final Sound SUMMONED = Sound.sound(Key.key("entity.zombie_villager.cure"), Sound.Source.HOSTILE, 1f, 2f);
    private static final double SPAWN_SPREAD = 1.5;
    private static final double SPAWN_RISE_MIN = 1.9;
    private static final double SPAWN_RISE_MAX = 3.0;

    private final DungeonHost host;
    private final PlacedRoom room;
    private final CatacombsFloor floor;
    private final Runnable onCleared;
    private final AtomicBoolean started = new AtomicBoolean();
    private final List<CatacombsMob> spawned = new ArrayList<>();
    private WatcherConfig config;
    private WatcherWaves waves;
    private WatcherEntity watcher;
    private BossBar bar;
    private Task ticker;
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
        config = WatcherConfig.shared();
        waves = new WatcherWaves(config, WatcherSpawns.mobCount(floor), ThreadLocalRandom.current());
        Pos home = RoomFeatures.shared().watcher(room).map(spot -> new Pos(spot.x(), spot.y(), spot.z()))
                .orElseGet(() -> host.arena().roomCentre(room).add(0, 4, 0));
        List<Pos> stops = RoomFeatures.shared().watcherStops(room).stream().map(spot -> new Pos(spot.x(), spot.y(), spot.z())).toList();
        watcher = new WatcherEntity(host.instance(), home, stops, config, ThreadLocalRandom.current());
        bar = BossBar.bossBar(Text.parse(config.bossBar()).asComponent(), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        ticker = MinecraftServer.getSchedulerManager().buildTask(this::tick).repeat(TaskSchedule.nextTick()).schedule();
        return true;
    }

    private synchronized void tick() {
        if (finished) return;
        watcher.tick();
        perform(waves.tick());
        if (waves.now() % 20 == 0 && waves.now() >= config.bossBarTicks()) refreshBar();
    }

    private void perform(List<WatcherWaves.Action> actions) {
        for (WatcherWaves.Action action : actions) {
            switch (action) {
                case WatcherWaves.Say(String line) -> {
                    host.broadcast(CatacombsMessages.watcher(line));
                    watcher.say(line);
                }
                case WatcherWaves.FlyOut() -> watcher.flyOut();
                case WatcherWaves.Summon() -> summon();
                case WatcherWaves.ShowBar() -> {
                    refreshBar();
                    host.alivePlayers().forEach(player -> player.showBossBar(bar));
                }
                case WatcherWaves.OpenPortal() -> complete();
            }
        }
    }

    private void refreshBar() {
        bar.name(Text.parse(waves.barTitle(config.bossBar())).asComponent());
        bar.progress(Math.clamp(waves.progress(), 0f, 1f));
    }

    private void summon() {
        DungeonMobDefinition base = DungeonMobRegistry.defaults().mob(SUMMON_ID);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        MobLooks.WatcherMobs pool = MobLooks.shared().watcherMobs().orElse(null);
        CatacombsMob mob;
        if (pool != null) {
            MobLooks.WatcherMobs.Summon summon = pool.roll(random);
            DungeonMobDefinition definition = new DungeonMobDefinition(base.id(), summon.look().displayName(), base.minimumFloor(),
                    base.role(), summon.health(), base.damage(), base.defense(), base.abilities());
            mob = new CatacombsMob(definition, room.index(), false, this, summon.look());
        } else {
            mob = new CatacombsMob(base, room.index(), false, this);
        }
        Pos home = watcher.home();
        Pos at = new Pos(home.x() + random.nextDouble(-SPAWN_SPREAD, SPAWN_SPREAD),
                home.y() + random.nextDouble(SPAWN_RISE_MIN, SPAWN_RISE_MAX),
                home.z() + random.nextDouble(-SPAWN_SPREAD, SPAWN_SPREAD));
        spawned.add(mob);
        mob.setInstance(host.instance(), at);
        host.instance().playSound(SUMMONED, at.x(), at.y(), at.z());
        watcher.flyHome();
    }

    @Override
    public synchronized void mobDied(CatacombsMob mob) {
        if (finished || waves == null) return;
        perform(waves.killed());
        refreshBar();
    }

    /** Staff clear: kills what is left and ends the room at once. */
    synchronized void finish() {
        if (finished) return;
        if (waves == null) {
            finished = true;
            host.state().markRoom(room.index(), DungeonRoomState.COMPLETED);
            host.later(1, onCleared);
            return;
        }
        complete();
    }

    private void complete() {
        finished = true;
        spawned.forEach(CatacombsMob::remove);
        if (watcher != null) watcher.remove();
        if (bar != null) bar.progress(0f);
        if (ticker != null) ticker.cancel();
        host.state().markRoom(room.index(), DungeonRoomState.COMPLETED);
        host.later(1, onCleared);
    }

    void hideBar() {
        if (bar == null) return;
        host.alivePlayers().forEach(player -> player.hideBossBar(bar));
    }

    void dispose() {
        if (ticker != null) ticker.cancel();
        if (watcher != null) watcher.remove();
        spawned.forEach(CatacombsMob::remove);
        if (bar != null) MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.hideBossBar(bar));
    }

    boolean started() {
        return started.get();
    }

    List<RoomFeatures.Spot> blessingSpots() {
        return RoomFeatures.shared().blessings(room);
    }
}
