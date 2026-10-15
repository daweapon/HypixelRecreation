package dev.daweapon.type.skyblockgeneric.crimson.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.crimson.SulphurOres;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMatcho;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Each Coal Block an eruption flings becomes a Matcho (wiki: Blazing_Volcano). Coal Blocks are as common as the
 * Sponges that become Sulphur Ore, but a Matcho is a full mob, so a fifth of the ore count lands as Matchos,
 * capped at 25, and unkilled ones crumble after 30 minutes (both estimates).
 */
public final class MatchoEruptions {
    static final int MAX_PER_ERUPTION = 25;
    private static final Duration LIFETIME = Duration.ofMinutes(30);
    private static final List<MobMatcho> ALIVE = new ArrayList<>();

    private MatchoEruptions() {
    }

    public static int countFor(VolcanoExplosivity explosivity) {
        return Math.min(MAX_PER_ERUPTION, Math.max(1, SulphurOres.countFor(explosivity) / 5));
    }

    public static synchronized void erupt(Instance instance, int count) {
        if (instance == null || count <= 0) return;
        ALIVE.removeIf(matcho -> matcho.isDead() || matcho.isRemoved());
        List<Pos> slopes = CrimsonMobSpawner.volcanoSlopes();
        List<MobMatcho> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Pos at = slopes.get(ThreadLocalRandom.current().nextInt(slopes.size()));
            if (!instance.isChunkLoaded(at)) instance.loadChunk(at).join();
            MobMatcho matcho = new MobMatcho();
            matcho.setInstance(instance, at);
            spawned.add(matcho);
        }
        ALIVE.addAll(spawned);
        MinecraftServer.getSchedulerManager().buildTask(() -> spawned.stream()
                        .filter(matcho -> !matcho.isDead() && !matcho.isRemoved() && matcho.getDamageDealt().isEmpty())
                        .forEach(MobMatcho::remove))
                .delay(TaskSchedule.millis(LIFETIME.toMillis())).schedule();
    }
}
