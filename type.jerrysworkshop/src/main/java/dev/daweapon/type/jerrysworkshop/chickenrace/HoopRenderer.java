package dev.daweapon.type.jerrysworkshop.chickenrace;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Draws the next hoop as a ring of snow-white particles (green for time hoops and the finish) and the
 * one after it fainter, for the racing player only.
 */
final class HoopRenderer {
    private static final double POINTS_PER_BLOCK = 2.2;
    private static final RGBLike SNOW = new Color(255, 255, 255);
    private static final RGBLike GREEN = new Color(85, 255, 85);
    private static final RGBLike FAINT_SNOW = new Color(200, 205, 215);
    private static final RGBLike FAINT_GREEN = new Color(120, 200, 120);

    private HoopRenderer() {
    }

    static void render(SkyBlockPlayer player, List<ChickenRaceCourse.Hoop> gates, int next) {
        if (next < gates.size()) {
            ChickenRaceCourse.Hoop hoop = gates.get(next);
            ring(player, hoop, hoop.timeHoop() ? GREEN : SNOW, 1.7f);
        }
        if (next + 1 < gates.size()) {
            ChickenRaceCourse.Hoop hoop = gates.get(next + 1);
            ring(player, hoop, hoop.timeHoop() ? FAINT_GREEN : FAINT_SNOW, 0.9f);
        }
    }

    private static void ring(SkyBlockPlayer player, ChickenRaceCourse.Hoop hoop, RGBLike colour, float scale) {
        Vec normal = hoop.normal();
        Vec side = normal.cross(new Vec(0, 1, 0));
        side = side.isZero() ? new Vec(1, 0, 0) : side.normalize();
        Vec up = side.cross(normal).normalize();
        Particle particle = Particle.DUST.withProperties(colour, scale);
        int points = (int) Math.ceil(2 * Math.PI * hoop.radius() * POINTS_PER_BLOCK);
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Vec point = hoop.center().add(side.mul(Math.cos(angle) * hoop.radius()))
                    .add(up.mul(Math.sin(angle) * hoop.radius()));
            player.sendPacket(new ParticlePacket(particle, true, true, point.x(), point.y(), point.z(), 0, 0, 0, 0, 1));
        }
    }
}
