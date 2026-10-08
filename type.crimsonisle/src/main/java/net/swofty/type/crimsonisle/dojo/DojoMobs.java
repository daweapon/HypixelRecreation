package net.swofty.type.crimsonisle.dojo;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/** Harmless arena mobs: they wander toward the player but never attack. */
public final class DojoMobs {
    private DojoMobs() {
    }

    public static EntityCreature zombie(@Nullable Material helmet, boolean baby, @Nullable String nameMarkup) {
        EntityCreature zombie = new EntityCreature(EntityType.ZOMBIE);
        if (helmet != null) zombie.setEquipment(EquipmentSlot.HELMET, ItemStack.of(helmet));
        if (baby) ((ZombieMeta) zombie.getEntityMeta()).setBaby(true);
        if (nameMarkup != null) {
            zombie.setCustomName(Text.of(nameMarkup).asComponent());
            zombie.setCustomNameVisible(true);
        }
        return zombie;
    }

    /** Walks a mob toward a point at {@code speed} blocks per second, keeping its fall speed. */
    public static void walkToward(EntityCreature mob, Pos target, double speed, double stopDistance) {
        Pos position = mob.getPosition();
        Vec offset = new Vec(target.x() - position.x(), 0, target.z() - position.z());
        if (offset.length() < stopDistance) {
            mob.setVelocity(new Vec(0, mob.getVelocity().y(), 0));
        } else {
            Vec step = offset.normalize().mul(speed);
            mob.setVelocity(new Vec(step.x(), mob.getVelocity().y(), step.z()));
        }
        mob.setView((float) Math.toDegrees(Math.atan2(-offset.x(), offset.z())), 0);
    }

    /** Knocks an entity away from {@code source} horizontally with an upward lift. */
    public static void knockAway(Entity entity, Pos source, double horizontal, double vertical) {
        Pos position = entity.getPosition();
        Vec direction = new Vec(position.x() - source.x(), 0, position.z() - source.z());
        if (direction.length() < 1e-3) {
            double yaw = Math.toRadians(source.yaw());
            direction = new Vec(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        Vec push = direction.normalize().mul(horizontal);
        entity.setVelocity(new Vec(push.x(), vertical, push.z()));
    }

    public static Pos randomOnDisc(double minRadius, double maxRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2);
        double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
        return new Pos(DojoArena.CENTER_X + Math.cos(angle) * radius, DojoArena.STAND_Y,
                DojoArena.CENTER_Z + Math.sin(angle) * radius);
    }
}
