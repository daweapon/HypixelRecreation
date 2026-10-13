package net.swofty.type.crystalhollows.command;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.crystalhollows.layout.StructureCatalogue;
import net.swofty.type.crystalhollows.layout.StructurePlacement;
import net.swofty.type.crystalhollows.layout.StructureSpec;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HollowsDebugMarkers {
    private static final double RANGE = 128;
    private static final int COLUMN_HEIGHT = 6;
    private static final Set<UUID> VIEWERS = ConcurrentHashMap.newKeySet();
    private static boolean running;

    private HollowsDebugMarkers() {
    }

    public static synchronized boolean toggle(SkyBlockPlayer player) {
        if (!VIEWERS.remove(player.getUuid())) {
            VIEWERS.add(player.getUuid());
            start();
            return true;
        }
        return false;
    }

    private static void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (!VIEWERS.isEmpty()) {
                try {
                    tick();
                } catch (RuntimeException ignored) {
                }
            }
            return TaskSchedule.seconds(1);
        });
    }

    private static void tick() {
        Map<String, StructurePlacement> placements = CrystalHollowsLayout.placements();
        var specs = StructureCatalogue.load();
        for (UUID id : VIEWERS) {
            Player viewer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id);
            if (viewer == null) {
                VIEWERS.remove(id);
                continue;
            }
            String nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (StructureSpec spec : specs) {
                StructurePlacement placed = placements.get(spec.id());
                for (Pos candidate : spec.candidates()) {
                    boolean isPlaced = placed != null && placed.origin().equals(candidate);
                    mark(viewer, candidate, isPlaced ? Particle.FLAME : Particle.ELECTRIC_SPARK);
                }
                if (placed == null) continue;
                for (Map.Entry<String, Pos> anchor : placed.anchors().entrySet()) {
                    mark(viewer, anchor.getValue(), Particle.HAPPY_VILLAGER);
                    double distance = viewer.getPosition().distance(anchor.getValue());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = spec.id() + "/" + anchor.getKey();
                    }
                }
            }
            if (nearest != null) {
                viewer.sendActionBar(Component.text("Nearest anchor: " + nearest + " (" + Math.round(nearestDistance) + " blocks)"));
            }
        }
    }

    private static void mark(Player viewer, Point at, Particle particle) {
        if (viewer.getPosition().distance(at) > RANGE) return;
        for (int height = 0; height < COLUMN_HEIGHT; height++) {
            viewer.sendPacket(new ParticlePacket(particle, true, true, at.x(), at.y() + height, at.z(), 0, 0, 0, 0, 1));
        }
    }
}
