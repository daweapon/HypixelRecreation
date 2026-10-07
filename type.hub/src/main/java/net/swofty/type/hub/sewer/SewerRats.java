package net.swofty.type.hub.sewer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobSewerRat;

import java.time.Duration;
import java.util.List;

/**
 * Keeps one Rat at each Sewer spawn point. A killed Rat comes back after 3 SkyBlock days
 * (60 real minutes), as on Hypixel.
 */
public final class SewerRats {
    private static final long RESPAWN_MILLIS = Duration.ofMinutes(60).toMillis();
    private static final List<SpawnPoint> SPAWN_POINTS = List.of(
            new SpawnPoint(new Pos(17.5, 65, -45.5)),
            new SpawnPoint(new Pos(17.5, 65, -30.5)),
            new SpawnPoint(new Pos(1.5, 61, -45.5)),
            new SpawnPoint(new Pos(-10.5, 61, -45.5)),
            new SpawnPoint(new Pos(-22.5, 63, -33.5)));

    private SewerRats() {}

    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null) SPAWN_POINTS.forEach(point -> point.tick(instance));
            return TaskSchedule.seconds(5);
        });
    }

    private static final class SpawnPoint {
        private final Pos position;
        private MobSewerRat rat;
        private long respawnAt;

        private SpawnPoint(Pos position) {
            this.position = position;
        }

        private void tick(Instance instance) {
            long now = System.currentTimeMillis();
            if (rat != null) {
                if (!rat.isRemoved() && !rat.isDead()) return;
                rat = null;
                respawnAt = now + RESPAWN_MILLIS;
            }
            if (now < respawnAt) return;
            if (!instance.isChunkLoaded(position)) instance.loadChunk(position).join();
            rat = new MobSewerRat();
            rat.setInstance(instance, position);
        }
    }
}
