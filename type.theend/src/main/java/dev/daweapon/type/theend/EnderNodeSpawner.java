package dev.daweapon.type.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.theend.EnderNodeType;
import dev.daweapon.type.skyblockgeneric.theend.EnderNodes;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Keeps 18 End Stone and 18 Obsidian Ender Nodes on the island: every 5 ticks a missing node is placed on a
 * random free spot, matching the staff description on the wiki.
 */
public final class EnderNodeSpawner {
    public static final int REFILL_TICKS = 5;
    public static final int PARTICLE_TICKS = 10;
    private static final int ATTEMPTS = 8;

    private static final Map<EnderNodeType, List<Pos>> SPOTS = new EnumMap<>(EnderNodeType.class);

    private EnderNodeSpawner() {
    }

    public static void start() {
        for (EnderNodeType type : EnderNodeType.values()) SPOTS.put(type, EnderNodeSpots.of(type));
        MinecraftServer.getSchedulerManager().buildTask(EnderNodeSpawner::refill)
                .repeat(TaskSchedule.tick(REFILL_TICKS)).schedule();
        MinecraftServer.getSchedulerManager().buildTask(EnderNodeSpawner::sparkle)
                .repeat(TaskSchedule.tick(PARTICLE_TICKS)).schedule();
    }

    private static void refill() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return;
        EnderNodes.prune(instance);
        for (EnderNodeType type : EnderNodeType.values()) {
            if (EnderNodes.count(type) >= EnderNodes.PER_TYPE) continue;
            List<Pos> spots = SPOTS.get(type);
            for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
                Pos spot = spots.get(ThreadLocalRandom.current().nextInt(spots.size()));
                if (EnderNodes.place(instance, spot, type)) break;
            }
        }
    }

    private static void sparkle() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || instance.getPlayers().isEmpty()) return;
        for (EnderNodes.Node node : EnderNodes.nodes()) {
            Pos at = node.position();
            instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, false, false,
                    at.x() + 0.5, at.y() + 0.5, at.z() + 0.5, 0.45f, 0.45f, 0.45f, 0.2f, 6));
        }
    }
}
