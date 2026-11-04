package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobArachnesKeeper;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobBroodmother;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobDasherSpider;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobFlintSkeleton;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobRainSlime;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobSpiderJockey;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobSplitterSpider;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobWeaverSpider;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Keeps the Spider's Den mobs topped up inside their zones' boxes (Minestom.regions.yml). Hypixel publishes no
 * caps or respawn rates, so every count is an estimate, except the Broodmother's 2.5 minute respawn (wiki
 * 0.27.2) and the Keeper's 1 minute respawn at each of its 9 wiki spots. Mobs only spawn near a player, and never inside a smaller zone nested in their own.
 */
public final class SpiderDenMobSpawner {
    private static final int TICK_SECONDS = 2;
    private static final double PLAYER_RANGE = 100;
    private static final double MIN_DISTANCE = 10;
    private static final int PLACEMENT_TRIES = 12;
    static final Pos BROODMOTHER_SPOT = new Pos(-189.5, 176, -310.5);
    public static final Pos SUMMIT = BROODMOTHER_SPOT;
    static final long BROODMOTHER_RESPAWN_MILLIS = 150_000;
    static final double TOXIC_SLIME_CHANCE = 20;
    static final long KEEPER_RESPAWN_MILLIS = 60_000;
    static final List<Pos> KEEPER_SPOTS = List.of(new Pos(-292.5, 47, -167.5), new Pos(-291.5, 47, -183.5), new Pos(-282.5, 47, -195.5),
            new Pos(-262.5, 49, -191.5), new Pos(-269.5, 47, -166.5), new Pos(-269.5, 61, -159.5), new Pos(-311.5, 43, -232.5),
            new Pos(-208.5, 44, -259.5), new Pos(-230.5, 57, -307.5));
    static final int[] GRANDMA_MOUND_BASE = {-300, 60, -215, -255, 100, -165};
    static final int[] CRIB_MOUND_BASE = {-285, 60, -300, -250, 92, -270};

    private static final List<Plan> PLANS = new ArrayList<>();
    private static boolean running;

    private SpiderDenMobSpawner() {
    }

    static {
        zone(RegionType.SPIDER_MOUND, 6, () -> dasher(false));
        zone(RegionType.SPIDER_MOUND, 6, () -> new MobSplitterSpider(SpiderDenMobs.SPLITTER_MOUND));
        zone(RegionType.SPIDER_MOUND, 6, () -> new MobWeaverSpider(SpiderDenMobs.WEAVER_MOUND));
        zone(RegionType.SPIDER_MOUND, 3, () -> voracious(false));
        zone(RegionType.SPIDER_MOUND, 4, () -> new MobSpiderJockey(SpiderDenMobs.JOCKEY_MOUND, SpiderDenMobs.JOCKEY_SKELETON_MOUND));
        zone(RegionType.ARACHNES_BURROW, 6, () -> dasher(true));
        zone(RegionType.ARACHNES_BURROW, 6, () -> new MobSplitterSpider(pick(SpiderDenMobs.SPLITTER_BURROW)));
        zone(RegionType.ARACHNES_BURROW, 6, () -> new MobWeaverSpider(pick(SpiderDenMobs.WEAVER_BURROW)));
        zone(RegionType.ARACHNES_BURROW, 4, () -> voracious(true));
        zone(RegionType.ARACHNES_BURROW, 4, () -> new MobSpiderJockey(SpiderDenMobs.JOCKEY_BURROW, SpiderDenMobs.JOCKEY_SKELETON_BURROW));
        zone(RegionType.GRAVEL_MINES, 8, MobFlintSkeleton::new);
        PLANS.add(new Plan(null, 1, MobBroodmother::new, BROODMOTHER_SPOT, BROODMOTHER_RESPAWN_MILLIS, () -> true, null));
        for (Pos spot : KEEPER_SPOTS) PLANS.add(new Plan(null, 1, MobArachnesKeeper::new, spot, KEEPER_RESPAWN_MILLIS, () -> true, null));
        rain(GRANDMA_MOUND_BASE, 5);
        rain(CRIB_MOUND_BASE, 5);
    }

    private static SpiderDenMob dasher(boolean burrow) {
        return new MobDasherSpider(burrow ? pick(SpiderDenMobs.DASHER_BURROW) : SpiderDenMobs.DASHER_MOUND);
    }

    private static SpiderDenMob voracious(boolean burrow) {
        return new SpiderDenMob(burrow ? pick(SpiderDenMobs.VORACIOUS_BURROW) : SpiderDenMobs.VORACIOUS_MOUND);
    }

    static <T> T pick(List<T> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    private static void zone(RegionType region, int count, Supplier<SpiderDenMob> factory) {
        PLANS.add(new Plan(region, count, factory, null, 0, () -> true, null));
    }

    private static void rain(int[] box, int count) {
        PLANS.add(new Plan(null, count, SpiderDenMobSpawner::rainSlime, null, 0, SpiderDenWeather::rainingHere, box));
    }

    static SpiderDenMob rainSlime() {
        boolean toxic = SpiderDenWeather.thunderingHere() && LootRoll.roll(null, null, TOXIC_SLIME_CHANCE);
        return new MobRainSlime(toxic ? SpiderDenMobs.TOXIC_RAIN_SLIME : SpiderDenMobs.RAIN_SLIME, toxic);
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().buildTask(SpiderDenMobSpawner::tick)
                .repeat(TaskSchedule.seconds(TICK_SECONDS)).schedule();
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || instance.getPlayers().isEmpty()) return;
        SpiderDenRelics.ensureSpawned(instance);
        PLANS.forEach(plan -> plan.tick(instance));
    }

    private static final class Plan {
        private final RegionType region;
        private final int count;
        private final Supplier<SpiderDenMob> factory;
        private final Pos fixed;
        private final long respawnMillis;
        private final BooleanSupplier gate;
        private final int[] box;
        private final List<SpiderDenMob> alive = new ArrayList<>();
        private long respawnAt;

        private Plan(RegionType region, int count, Supplier<SpiderDenMob> factory, Pos fixed, long respawnMillis,
                     BooleanSupplier gate, int[] box) {
            this.region = region;
            this.count = count;
            this.factory = factory;
            this.fixed = fixed;
            this.respawnMillis = respawnMillis;
            this.gate = gate;
            this.box = box;
        }

        private void tick(Instance instance) {
            boolean hadMobs = !alive.isEmpty();
            Iterator<SpiderDenMob> iterator = alive.iterator();
            while (iterator.hasNext()) {
                SpiderDenMob mob = iterator.next();
                if (mob.isDead() || mob.isRemoved()) iterator.remove();
            }
            if (hadMobs && alive.isEmpty() && respawnMillis > 0) respawnAt = System.currentTimeMillis() + respawnMillis;
            if (!gate.getAsBoolean()) {
                alive.stream().filter(mob -> mob.getDamageDealt().isEmpty()).forEach(SpiderDenMob::remove);
                return;
            }
            if (alive.size() >= count || System.currentTimeMillis() < respawnAt) return;
            Pos spot = fixed != null ? fixed : findSpot(instance);
            if (spot == null || !playerNear(instance, spot)) return;
            if (!instance.isChunkLoaded(spot)) instance.loadChunk(spot).join();
            SpiderDenMob mob = factory.get();
            mob.setInstance(instance, spot);
            alive.add(mob);
        }

        private Pos findSpot(Instance instance) {
            List<Integer> bounds = bounds();
            if (bounds == null) return null;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int attempt = 0; attempt < PLACEMENT_TRIES; attempt++) {
                int x = random.nextInt(bounds.get(0), bounds.get(1) + 1);
                int z = random.nextInt(bounds.get(4), bounds.get(5) + 1);
                if (!instance.isChunkLoaded(x >> 4, z >> 4)) continue;
                Pos ground = groundAt(instance, x, z, bounds.get(2), bounds.get(3));
                if (ground == null) continue;
                if (box != null) return ground;
                SkyBlockRegion at = SkyBlockRegion.getRegionOfPosition(ground);
                if (at != null && at.getType() == region) return ground;
            }
            return null;
        }

        private List<Integer> bounds() {
            if (box != null) return List.of(box[0], box[3], box[1], box[4], box[2], box[5]);
            SkyBlockRegion zone = SkyBlockRegion.getRegions().stream().filter(candidate -> candidate.getType() == region).findFirst().orElse(null);
            return zone == null ? null : zone.getBounds();
        }

        private static Pos groundAt(Instance instance, int x, int z, int minY, int maxY) {
            for (int y = maxY; y >= minY; y--) {
                Block floor = instance.getBlock(x, y, z);
                if (floor.isSolid() && !floor.isLiquid() && instance.getBlock(x, y + 1, z).isAir() && instance.getBlock(x, y + 2, z).isAir()) {
                    return new Pos(x + 0.5, y + 1, z + 0.5);
                }
            }
            return null;
        }

        private static boolean playerNear(Instance instance, Pos spot) {
            for (Player player : instance.getPlayers()) {
                double distance = player.getPosition().distance(spot);
                if (distance >= MIN_DISTANCE && distance <= PLAYER_RANGE) return true;
            }
            return false;
        }
    }
}
