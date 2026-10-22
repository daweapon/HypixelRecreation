package dev.daweapon.type.thefarmingislands.mob;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn.MobBarnChicken;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn.MobBarnCow;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn.MobBarnPig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class BarnMobSpawner {
    private static final int ATTEMPTS = 12;
    private static final double LEASH = 40;
    private static final List<Rule> RULES = new ArrayList<>();
    private static boolean running;

    private BarnMobSpawner() {
    }

    static {
        Area barn = new Area(133, 150, -257, -244, 77, 68);
        Area pen = new Area(110, 126, -250, -241, 80, 66);
        rule(barn, 5, MobBarnCow::new);
        rule(barn, 4, MobBarnPig::new);
        rule(barn, 6, MobBarnChicken::new);
        rule(pen, 5, MobBarnCow::new);
        rule(pen, 4, MobBarnPig::new);
        rule(pen, 6, MobBarnChicken::new);
    }

    private static void rule(Area area, int count, Supplier<SkyBlockMob> factory) {
        RULES.add(new Rule(area, count, factory, new ArrayList<>()));
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null && !SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) {
                RULES.forEach(rule -> rule.tick(instance));
            }
            return TaskSchedule.seconds(5);
        });
    }

    private record Area(int minX, int maxX, int minZ, int maxZ, int topY, int bottomY) {
        Pos groundPoint(Instance instance) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
                int x = random.nextInt(minX, maxX + 1);
                int z = random.nextInt(minZ, maxZ + 1);
                if (!instance.isChunkLoaded(x >> 4, z >> 4)) instance.loadChunk(x >> 4, z >> 4).join();
                for (int y = topY; y >= bottomY; y--) {
                    if (instance.getBlock(x, y, z).isSolid()
                            && instance.getBlock(x, y + 1, z).isAir()
                            && instance.getBlock(x, y + 2, z).isAir()) {
                        return new Pos(x + 0.5, y + 1, z + 0.5, random.nextFloat() * 360 - 180, 0);
                    }
                }
            }
            return null;
        }
    }

    private record Rule(Area area, int count, Supplier<SkyBlockMob> factory, List<Spawned> alive) {
        void tick(Instance instance) {
            Iterator<Spawned> iterator = alive.iterator();
            while (iterator.hasNext()) {
                Spawned spawned = iterator.next();
                SkyBlockMob mob = spawned.mob();
                if (mob.isDead() || mob.isRemoved()) {
                    iterator.remove();
                } else if (mob.getPosition().distance(spawned.home()) > LEASH) {
                    mob.remove();
                    iterator.remove();
                }
            }
            if (alive.size() >= count) return;
            Pos home = area.groundPoint(instance);
            if (home == null) return;
            SkyBlockMob mob = factory.get();
            mob.setInstance(instance, home);
            alive.add(new Spawned(mob, home));
        }
    }

    private record Spawned(SkyBlockMob mob, Pos home) {
    }
}
