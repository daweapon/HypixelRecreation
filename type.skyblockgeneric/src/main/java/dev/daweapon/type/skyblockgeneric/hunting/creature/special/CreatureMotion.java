package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;

import java.util.Comparator;
import java.util.Optional;

final class CreatureMotion {
    private static final double LINE_STEP = 0.5;

    private CreatureMotion() {
    }

    static Pos stepToward(Pos from, Point to, double distance) {
        Vec delta = Vec.fromPoint(to.sub(from));
        double length = delta.length();
        if (length <= distance || length < 1e-6) return new Pos(to.x(), to.y(), to.z(), from.yaw(), from.pitch());
        Vec step = delta.mul(distance / length);
        return from.add(step.x(), step.y(), step.z());
    }

    static boolean lookingAt(Pos viewer, double eyeHeight, Point target, double minDot) {
        Vec toTarget = Vec.fromPoint(target.sub(viewer.add(0, eyeHeight, 0)));
        if (toTarget.lengthSquared() < 1e-9) return true;
        return toTarget.normalize().dot(viewer.direction()) >= minDot;
    }

    static boolean lookingAt(SkyBlockPlayer player, Point target, double minDot) {
        return lookingAt(player.getPosition(), player.getEyeHeight(), target, minDot);
    }

    static Vec horizontal(Point from, Point to) {
        Vec flat = new Vec(to.x() - from.x(), 0, to.z() - from.z());
        return flat.lengthSquared() < 1e-9 ? Vec.ZERO : flat.normalize();
    }

    static Optional<SkyBlockPlayer> nearestPlayer(HuntingCreatureMob mob, double range) {
        if (mob.getInstance() == null) return Optional.empty();
        double limit = range * range;
        return SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(player -> player.getInstance() == mob.getInstance() && !player.isDead())
                .filter(player -> player.getPosition().distanceSquared(mob.getPosition()) <= limit)
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(mob.getPosition())));
    }

    static boolean lineOfSight(Instance instance, Point from, Point to) {
        Vec delta = Vec.fromPoint(to.sub(from));
        double length = delta.length();
        if (length < 1e-6) return true;
        Vec step = delta.mul(LINE_STEP / length);
        Pos probe = new Pos(from.x(), from.y(), from.z());
        for (double travelled = LINE_STEP; travelled < length; travelled += LINE_STEP) {
            probe = probe.add(step.x(), step.y(), step.z());
            if (instance.getBlock(probe).isSolid()) return false;
        }
        return true;
    }

    static void fly(HuntingCreatureMob mob, Point target, double distance) {
        Instance instance = mob.getInstance();
        if (instance == null) return;
        Pos next = stepToward(mob.getPosition(), target, distance).withLookAt(target);
        mob.setVelocity(Vec.ZERO);
        if (instance.getBlock(next).isSolid()) return;
        mob.teleport(next);
    }

    static void walk(HuntingCreatureMob mob, Point target, double distance) {
        Instance instance = mob.getInstance();
        if (instance == null) return;
        mob.getVanillaNavigator().stop();
        Pos from = mob.getPosition();
        Vec direction = horizontal(from, target);
        if (direction.lengthSquared() == 0) return;
        Pos next = from.add(direction.x() * distance, 0, direction.z() * distance).withLookAt(from.add(direction));
        if (instance.getBlock(next).isSolid()) {
            Pos stepped = next.add(0, 1, 0);
            if (instance.getBlock(stepped).isSolid() || instance.getBlock(stepped.add(0, 1, 0)).isSolid()) return;
            next = stepped;
        }
        mob.setVelocity(new Vec(0, mob.getVelocity().y(), 0));
        mob.teleport(next);
    }

    static boolean strike(HuntingCreatureMob mob, SkyBlockPlayer player, long cooldownMillis) {
        long now = System.currentTimeMillis();
        if (mob.getLastAttack() + cooldownMillis > now) return false;
        mob.setLastAttack(now);
        MobAttackService.strike(mob, player);
        return true;
    }

    static void say(HuntingCreatureMob mob, SkyBlockPlayer player, String when) {
        for (CreatureSpec.Message message : mob.spec().messages()) {
            if (when.equals(message.when())) player.sendMessage(Text.parse(message.text()));
        }
    }
}
