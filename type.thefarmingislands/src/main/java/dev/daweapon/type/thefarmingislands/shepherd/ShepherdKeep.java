package dev.daweapon.type.thefarmingislands.shepherd;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ShepherdKeep {
    public static final Pos CENTER = new Pos(388.5, 85, -372.5);
    public static final int SHEEP_COUNT = 8;
    public static final long REGROW_MILLIS = 30_000;
    private static final double MIN_DISTANCE = 3;
    private static final double MAX_DISTANCE = 10;
    private static final int SCAN_TOP = 120;
    private static final int SCAN_BOTTOM = 60;
    private static final List<EntityKeepSheep> SHEEP = new ArrayList<>();
    private static boolean running;

    private ShepherdKeep() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null && !SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) tick(instance);
            return TaskSchedule.seconds(1);
        });
    }

    private static synchronized void tick(Instance instance) {
        long now = System.currentTimeMillis();
        SHEEP.removeIf(sheep -> sheep.isRemoved() || sheep.isDead());
        SHEEP.forEach(sheep -> sheep.regrowIfReady(now, REGROW_MILLIS));
        while (SHEEP.size() < SHEEP_COUNT) {
            Pos spot = groundNear(instance);
            if (spot == null) return;
            EntityKeepSheep sheep = new EntityKeepSheep();
            sheep.setInstance(instance, spot);
            SHEEP.add(sheep);
        }
    }

    private static Pos groundNear(Instance instance) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        double x = CENTER.x() + Math.cos(angle) * distance;
        double z = CENTER.z() + Math.sin(angle) * distance;
        Pos column = new Pos(x, CENTER.y(), z);
        if (!instance.isChunkLoaded(column)) instance.loadChunk(column).join();
        for (int y = SCAN_TOP; y >= SCAN_BOTTOM; y--) {
            if (instance.getBlock((int) Math.floor(x), y, (int) Math.floor(z)).isSolid()) return new Pos(x, y + 1, z);
        }
        return null;
    }

    public static synchronized boolean allSheared() {
        return SHEEP.size() >= SHEEP_COUNT && SHEEP.stream().allMatch(EntityKeepSheep::isSheared);
    }
}
