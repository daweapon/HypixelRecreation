package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EventSites {
    private static final int SCAN_UP = 10;
    private static final int SCAN_DOWN = 45;
    private static final int ATTEMPTS = 12;

    private EventSites() {
    }

    public static MiningEventConfig.Site pick(List<MiningEventConfig.Site> sites) {
        return sites.get(ThreadLocalRandom.current().nextInt(sites.size()));
    }

    public static Pos center(Instance instance, MiningEventConfig.Site site) {
        Pos ground = groundAt(instance, site.x(), site.y(), site.z());
        return ground != null ? ground : new Pos(site.x(), site.y(), site.z());
    }

    public static Pos near(Instance instance, MiningEventConfig.Site site) {
        return near(instance, site, site.radius());
    }

    public static Pos near(Instance instance, MiningEventConfig.Site site, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            Pos ground = groundAt(instance, site.x() + Math.cos(angle) * distance, site.y(), site.z() + Math.sin(angle) * distance);
            if (ground != null) return ground;
        }
        return center(instance, site);
    }

    public static @Nullable Pos groundAt(Instance instance, double x, double referenceY, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        if (instance.getChunkAt(blockX, blockZ) == null) instance.loadChunk(blockX >> 4, blockZ >> 4).join();
        for (int y = (int) referenceY + SCAN_UP; y > referenceY - SCAN_DOWN; y--) {
            if (!instance.getBlock(blockX, y, blockZ, Block.Getter.Condition.TYPE).isSolid()) continue;
            if (instance.getBlock(blockX, y + 1, blockZ, Block.Getter.Condition.TYPE).isSolid()) continue;
            if (instance.getBlock(blockX, y + 2, blockZ, Block.Getter.Condition.TYPE).isSolid()) continue;
            return new Pos(blockX + 0.5, y + 1, blockZ + 0.5);
        }
        return null;
    }
}
