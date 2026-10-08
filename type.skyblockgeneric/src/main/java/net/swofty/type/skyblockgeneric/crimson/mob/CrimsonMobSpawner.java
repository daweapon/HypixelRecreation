package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.*;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Keeps the Crimson Isle's regular mobs topped up at their spawn spots ({@link CrimsonSpawnPoints}). Hypixel
 * publishes no caps or respawn rates, so every count below is an estimate (spec 03 §1.1), except the 12 Flare
 * pillars and the 2 Hellwisps. Blazes only spawn from 17:00 to 09:00 and Ghasts from 19:00 to 06:00 SkyBlock
 * time; out of hours the idle ones leave.
 */
public final class CrimsonMobSpawner {
    static final double RARE_VARIANT_CHANCE = 5;
    private static final double LEASH = 48;
    private static final List<Rule> RULES = new ArrayList<>();
    private static boolean running;

    private CrimsonMobSpawner() {
    }

    static {
        rule(CrimsonSpawnPoints.STRONGHOLD_BLAZE, 12, Hours.NIGHT_BLAZE, MobBlaze::new);
        rule(CrimsonSpawnPoints.STRONGHOLD_MUTATED_BLAZE, 4, Hours.ALWAYS, MobMutatedBlaze::new);
        rule(CrimsonSpawnPoints.STRONGHOLD_WITHER_SKELETON, 8, Hours.ALWAYS, MobWitherSkeleton::new);
        rule(CrimsonSpawnPoints.STRONGHOLD_WITHER_SPECTRE, 8, Hours.ALWAYS, MobWitherSpectre::new);
        rule(CrimsonSpawnPoints.STRONGHOLD_BEZAL, 4, Hours.ALWAYS, MobBezal::new);
        rule(CrimsonSpawnPoints.SMOLDERING_TOMB_UPPER, 12, Hours.ALWAYS, MobSmolderingBlaze::new);
        rule(CrimsonSpawnPoints.SMOLDERING_TOMB_LOWER, 4, Hours.ALWAYS, MobMillenniaAgedBlaze::new);
        rule(CrimsonSpawnPoints.BURNING_DESERT, 10, Hours.ALWAYS, MobFlamingSpider::new);
        rule(CrimsonSpawnPoints.BURNING_DESERT, 10, Hours.ALWAYS, MobMagmaCubeRider::new);
        rule(CrimsonSpawnPoints.CRIMSON_FIELDS, 15, Hours.ALWAYS, MobMagmaCube::new);
        rule(CrimsonSpawnPoints.MAGMA_CAVES, 6, Hours.ALWAYS, MobPackMagmaCube::new);
        rule(CrimsonSpawnPoints.MAGMA_CAVES, 6, Hours.ALWAYS, MobKadaKnight::new);
        rule(CrimsonSpawnPoints.WASTELAND_SKY, 6, Hours.NIGHT_GHAST, MobGhast::new);
        rule(CrimsonSpawnPoints.WASTELAND_SKY, 4, Hours.NIGHT_GHAST, MobDiveGhast::new);
        rule(CrimsonSpawnPoints.MYSTIC_MARSH_BULLS, 8, Hours.ALWAYS, MobMushroomBull::new);
        rule(CrimsonSpawnPoints.MYSTIC_MARSH, 6, Hours.ALWAYS, MobExe::new);
        rule(CrimsonSpawnPoints.MAGE_OUTPOST, 10, Hours.ALWAYS,
                () -> rare() ? new MobKrondorNecromancer() : new MobFireMage());
        rule(CrimsonSpawnPoints.BARBARIAN_OUTPOST, 10, Hours.ALWAYS,
                () -> rare() ? new MobGoliathBarbarian() : new MobBarbarian());
        rule(CrimsonSpawnPoints.DRAGONTAIL_STREETS, 6, Hours.ALWAYS, MobBarbarianGuard::new);
        rule(CrimsonSpawnPoints.SCARLETON_STREETS, 6, Hours.ALWAYS, MobMageGuard::new);
        rule(CrimsonSpawnPoints.MATRIARCH_POOLS, 2, Hours.ALWAYS, MobHellwisp::new);
        rule(CrimsonSpawnPoints.VOLCANO_CEILINGS, 8, Hours.ALWAYS, MobCinderbat::new);
        for (Pos pillar : CrimsonSpawnPoints.FLARE_PILLARS) {
            rule(List.of(pillar), 1, Hours.ALWAYS, MobFlare::new);
        }
    }

    private static void rule(List<Pos> points, int count, Hours hours, Supplier<CrimsonIsleMob> factory) {
        RULES.add(new Rule(points, count, hours, factory, new ArrayList<>()));
    }

    private static boolean rare() {
        return LootRoll.roll(null, null, RARE_VARIANT_CHANCE);
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null && !SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) {
                int hour = SkyBlockCalendar.getHour();
                RULES.forEach(rule -> rule.tick(instance, hour));
            }
            return TaskSchedule.seconds(5);
        });
    }

    public static List<Pos> volcanoSlopes() {
        return CrimsonSpawnPoints.VOLCANO_SLOPES;
    }

    enum Hours {
        ALWAYS(0, 24),
        NIGHT_BLAZE(17, 9),
        NIGHT_GHAST(19, 6);

        private final int from;
        private final int until;

        Hours(int from, int until) {
            this.from = from;
            this.until = until;
        }

        boolean contains(int hour) {
            if (from < until) return hour >= from && hour < until;
            return hour >= from || hour < until;
        }
    }

    private record Rule(List<Pos> points, int count, Hours hours, Supplier<CrimsonIsleMob> factory,
                        List<Spawned> alive) {
        void tick(Instance instance, int hour) {
            boolean open = hours.contains(hour);
            Iterator<Spawned> iterator = alive.iterator();
            while (iterator.hasNext()) {
                Spawned spawned = iterator.next();
                CrimsonIsleMob mob = spawned.mob();
                if (mob.isDead() || mob.isRemoved()) {
                    iterator.remove();
                    continue;
                }
                boolean idle = mob.getDamageDealt().isEmpty();
                if ((!open && idle) || mob.getPosition().distance(spawned.home()) > LEASH) {
                    mob.remove();
                    iterator.remove();
                }
            }
            if (!open || alive.size() >= count || points.isEmpty()) return;
            Pos home = points.get(ThreadLocalRandom.current().nextInt(points.size()));
            if (!instance.isChunkLoaded(home)) instance.loadChunk(home).join();
            CrimsonIsleMob mob = factory.get();
            mob.setInstance(instance, home);
            alive.add(new Spawned(mob, home));
        }
    }

    private record Spawned(CrimsonIsleMob mob, Pos home) {
    }
}
