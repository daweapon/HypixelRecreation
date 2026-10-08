package net.swofty.type.jerrysworkshop.chickenrace;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Draws the next hoop brightly and the one after it faintly, for the racing player only. */
final class HoopRenderer {
    private static final int POINTS = 28;
    private static final RGBLike NEXT = new Color(255, 255, 255);
    private static final RGBLike GREEN = new Color(60, 230, 60);
    private static final RGBLike CLUCKPOINT = new Color(255, 200, 40);
    private static final RGBLike UPCOMING = new Color(150, 150, 150);

    private HoopRenderer() {
    }

    static void render(SkyBlockPlayer player, ChickenRaceCourse course, int nextHoop) {
        if (nextHoop < course.hoops().size()) {
            ChickenRaceCourse.Hoop hoop = course.hoops().get(nextHoop);
            ring(player, hoop, course.hoopRadius(), colour(hoop), 1.6f);
        }
        if (nextHoop + 1 < course.hoops().size()) {
            ring(player, course.hoops().get(nextHoop + 1), course.hoopRadius(), UPCOMING, 1.0f);
        }
    }

    private static RGBLike colour(ChickenRaceCourse.Hoop hoop) {
        if (hoop.cluckpoint() != null) return CLUCKPOINT;
        return hoop.green() ? GREEN : NEXT;
    }

    private static void ring(SkyBlockPlayer player, ChickenRaceCourse.Hoop hoop, double radius, RGBLike colour, float scale) {
        Vec normal = hoop.normal();
        Vec side = normal.cross(new Vec(0, 1, 0));
        side = side.isZero() ? new Vec(1, 0, 0) : side.normalize();
        Vec up = side.cross(normal).normalize();
        Particle particle = Particle.DUST.withProperties(colour, scale);
        for (int i = 0; i < POINTS; i++) {
            double angle = 2 * Math.PI * i / POINTS;
            Vec point = hoop.center().add(side.mul(Math.cos(angle) * radius)).add(up.mul(Math.sin(angle) * radius));
            player.sendPacket(new ParticlePacket(particle, true, true, point.x(), point.y(), point.z(), 0, 0, 0, 0, 1));
        }
        if (hoop.green()) {
            player.sendPacket(new ParticlePacket(Particle.HAPPY_VILLAGER, true, true,
                    hoop.center().x(), hoop.center().y(), hoop.center().z(), (float) radius / 2, (float) radius / 2,
                    (float) radius / 2, 0, 6));
        }
    }
}
