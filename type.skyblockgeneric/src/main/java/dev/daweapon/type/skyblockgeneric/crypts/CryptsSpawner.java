package dev.daweapon.type.skyblockgeneric.crypts;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobCryptGhoul;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobGoldenGhoul;
import dev.daweapon.type.skyblockgeneric.runecrafting.RunicMobs;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Spawns the Hub Crypts mobs at fixed spots, the way the wiki's spawn table describes them: every spot gains one
 * mob per spawn delay until it holds its cap. Crypt Ghouls follow the wiki (15 spots, 2 per spot or 4 during the
 * Spooky Festival, 14 seconds). Golden Ghouls only spawn in the two large rooms; their cap and delay are estimates.
 * Spots are open 3x3 floors picked from the Hub world inside the CRYPTS zone boxes.
 */
public final class CryptsSpawner {
    static final int CRYPT_GHOUL_CAP = 2;
    static final int CRYPT_GHOUL_SPOOKY_CAP = 4;
    static final long CRYPT_GHOUL_DELAY_MILLIS = 14_000;
    static final int GOLDEN_GHOUL_CAP = 2;
    static final long GOLDEN_GHOUL_DELAY_MILLIS = 30_000;

    static final List<Pos> CRYPT_GHOUL_SPOTS = List.of(
            spot(-103, 48, -106), spot(-163, 63, -111), spot(-47, 56, -136), spot(-131, 42, -147), spot(-87, 55, -140),
            spot(-100, 39, -76), spot(-128, 58, -127), spot(-134, 51, -100), spot(-71, 48, -110), spot(-145, 58, -112),
            spot(-62, 52, -126), spot(-114, 43, -87), spot(-81, 56, -119), spot(-88, 43, -88), spot(-102, 48, -126));

    static final List<Pos> GOLDEN_GHOUL_SPOTS = List.of(spot(-129, 42, -139), spot(-101, 39, -90));

    private static final List<Spot> SPOTS = new ArrayList<>();
    private static boolean running;

    static {
        CRYPT_GHOUL_SPOTS.forEach(position -> SPOTS.add(new Spot(position, MobCryptGhoul::new,
                CryptsSpawner::cryptGhoulCap, CRYPT_GHOUL_DELAY_MILLIS)));
        GOLDEN_GHOUL_SPOTS.forEach(position -> SPOTS.add(new Spot(position, MobGoldenGhoul::new,
                () -> GOLDEN_GHOUL_CAP, GOLDEN_GHOUL_DELAY_MILLIS)));
    }

    private CryptsSpawner() {
    }

    private static Pos spot(int x, int y, int z) {
        return new Pos(x + 0.5, y, z + 0.5);
    }

    static int cryptGhoulCap() {
        return cryptGhoulCap(SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL));
    }

    static int cryptGhoulCap(boolean spookyFestival) {
        return spookyFestival ? CRYPT_GHOUL_SPOOKY_CAP : CRYPT_GHOUL_CAP;
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().buildTask(CryptsSpawner::tick)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || instance.getPlayers().isEmpty()) return;
        long now = System.currentTimeMillis();
        SPOTS.forEach(spot -> spot.tick(instance, now));
    }

    static final class SpawnClock {
        private final long delayMillis;
        private long nextSpawnAt;

        SpawnClock(long delayMillis) {
            this.delayMillis = delayMillis;
        }

        boolean due(int alive, int cap, long now) {
            if (alive >= cap || now < nextSpawnAt) return false;
            nextSpawnAt = now + delayMillis;
            return true;
        }
    }

    private static final class Spot {
        private final Pos position;
        private final Supplier<CryptMob> factory;
        private final IntSupplier cap;
        private final SpawnClock clock;
        private final List<CryptMob> alive = new ArrayList<>();

        private Spot(Pos position, Supplier<CryptMob> factory, IntSupplier cap, long delayMillis) {
            this.position = position;
            this.factory = factory;
            this.cap = cap;
            this.clock = new SpawnClock(delayMillis);
        }

        private void tick(Instance instance, long now) {
            alive.removeIf(mob -> mob.isDead() || mob.isRemoved());
            if (!clock.due(alive.size(), cap.getAsInt(), now)) return;
            if (!instance.isChunkLoaded(position)) instance.loadChunk(position).join();
            CryptMob mob = factory.get();
            RunicMobs.rollSpawn(mob);
            mob.setInstance(instance, position);
            alive.add(mob);
        }
    }
}
