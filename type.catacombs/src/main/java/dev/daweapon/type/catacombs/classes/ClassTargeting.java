package dev.daweapon.type.catacombs.classes;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/** Line-of-sight helpers for class abilities: what a player is looking at, and who stands near a point. */
public final class ClassTargeting {
    private static final double STEP = 0.5;
    private static final double HIT_RADIUS = 1.2;

    private ClassTargeting() {
    }

    public record Hit(Point point, @Nullable Entity entity) {
    }

    public static Pos eye(Player player) {
        return player.getPosition().add(0, player.getEyeHeight(), 0);
    }

    public static Vec direction(Pos from, double yawOffset) {
        return Pos.ZERO.withView(from.yaw() + (float) yawOffset, from.pitch()).direction();
    }

    /** Walks from the eye along the view until it reaches an entity matching {@code target}, a solid block or the range. */
    public static Hit trace(Player player, double range, double yawOffset, Predicate<Entity> target) {
        Instance instance = player.getInstance();
        Pos eye = eye(player);
        Vec direction = direction(player.getPosition(), yawOffset);
        Point point = eye;
        for (double travelled = STEP; travelled <= range; travelled += STEP) {
            point = eye.add(direction.mul(travelled));
            if (instance.getBlock(point).isSolid()) return new Hit(point.sub(direction.mul(STEP)), null);
            for (Entity entity : instance.getNearbyEntities(point, HIT_RADIUS)) {
                if (entity != player && target.test(entity)) return new Hit(point, entity);
            }
        }
        return new Hit(point, null);
    }

    public static Hit traceMob(Player player, double range, double yawOffset) {
        return trace(player, range, yawOffset, ClassTargeting::isLiveMob);
    }

    public static boolean isLiveMob(Entity entity) {
        return entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved();
    }

    public static List<SkyBlockMob> mobsNear(Instance instance, Point center, double radius) {
        return instance.getNearbyEntities(center, radius).stream()
                .filter(ClassTargeting::isLiveMob)
                .map(SkyBlockMob.class::cast)
                .toList();
    }

    public static void trail(Instance instance, Point from, Point to, Particle particle) {
        Vec path = Vec.fromPoint(to.sub(from));
        double length = path.length();
        if (length == 0) return;
        Vec step = path.normalize().mul(STEP);
        Point point = from;
        for (double travelled = 0; travelled <= length; travelled += STEP) {
            particle(instance, point, particle, 1);
            point = point.add(step);
        }
    }

    public static void particle(Instance instance, Point at, Particle particle, int count) {
        instance.sendGroupedPacket(new ParticlePacket(particle, true, true, at.x(), at.y(), at.z(), 0.2f, 0.2f, 0.2f, 0f, count));
    }

    public static void ring(Instance instance, Point center, double radius, Particle particle) {
        int points = Math.max(12, (int) (radius * 8));
        for (int index = 0; index < points; index++) {
            double angle = 2 * Math.PI * index / points;
            particle(instance, center.add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius), particle, 1);
        }
    }
}
