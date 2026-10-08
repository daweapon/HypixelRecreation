package net.swofty.type.skyblockgeneric.fishing.hotspot;

import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Draws each active hotspot as a ring of particles on the liquid's surface for nearby players who have unlocked
 * hotspots.
 */
public final class FishingHotspotRenderer {
    private static final double VIEW_DISTANCE = 64;
    private static final int POINTS = 32;

    private FishingHotspotRenderer() {
    }

    public static void start(Scheduler scheduler) {
        scheduler.buildTask(FishingHotspotRenderer::render).repeat(TaskSchedule.tick(10)).schedule();
    }

    private static void render() {
        if (HypixelConst.getTypeLoader() == null) return;
        List<FishingHotspotService.ActiveHotspot> hotspots =
                FishingHotspotService.active(HypixelConst.getTypeLoader().getType().name(), System.currentTimeMillis());
        if (hotspots.isEmpty()) return;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (!FishingHotspotService.unlocked(player)) continue;
            for (FishingHotspotService.ActiveHotspot hotspot : hotspots) {
                if (player.getPosition().distance(hotspot.position()) > VIEW_DISTANCE) continue;
                for (int i = 0; i < POINTS; i++) {
                    double angle = Math.PI * 2 * i / POINTS;
                    player.sendPacket(new ParticlePacket(Particle.FLAME, true, true,
                            hotspot.position().x() + Math.cos(angle) * FishingHotspotService.RADIUS,
                            hotspot.position().y() + 0.1,
                            hotspot.position().z() + Math.sin(angle) * FishingHotspotService.RADIUS,
                            0f, 0f, 0f, 0f, 1));
                }
            }
        }
    }
}
