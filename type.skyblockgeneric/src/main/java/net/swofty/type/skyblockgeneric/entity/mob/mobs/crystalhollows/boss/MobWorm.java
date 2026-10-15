package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * A Crystal Hollows worm: a chain of head-textured armor stands that tunnels through stone, chasing the nearest
 * player in a snaking path. Only the head can be hurt, and every hit costs it one health. The body length, turning,
 * undulation, chase range, bite rate and lifetime are estimates.
 */
public abstract class MobWorm extends CrystalHollowsBossMob {
    private static final int LIFETIME_TICKS = 20 * 90;
    private static final double CHASE_RANGE = 40;
    private static final double LEASH = 60;
    private static final double SPEED_DIVISOR = 450;
    private static final double TURN_RATE = 0.12;
    private static final double SNAKE_AMPLITUDE = 0.12;
    private static final double SNAKE_FREQUENCY = 0.2;
    private static final double BITE_RANGE = 2.2;
    private static final int BITE_INTERVAL = 20;
    private static final double AIM_HEIGHT = 1.0;

    private final Deque<Pos> trail = new ArrayDeque<>();
    private final WormBody body = new WormBody();
    private Vec heading = new Vec(1, 0, 0);
    private @Nullable Pos home;

    protected MobWorm(CrimsonMobProfile profile) {
        super(profile);
    }

    protected abstract String headTexture();

    @Override
    protected float hitDamage() {
        return 1;
    }

    @Override
    protected double lootShareDamage() {
        return 1;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        home = getPosition();
        setNoGravity(true);
        setInvisible(true);
        editEntityMeta(ArmorStandMeta.class, meta -> meta.setHasNoBasePlate(true));
        setHelmet(ItemStacks.head(headTexture(), "").build());
        double angle = Math.random() * Math.PI * 2;
        heading = new Vec(Math.cos(angle), 0, Math.sin(angle));
        body.spawn(getInstance(), getPosition(), headTexture());
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks > LIFETIME_TICKS) {
            remove();
            return;
        }
        Optional<SkyBlockPlayer> target = nearestPlayer(CHASE_RANGE, player -> true);
        steer(target.orElse(null), ticks);
        Pos next = getPosition().add(heading.mul(getProfile().speed() / SPEED_DIVISOR));
        teleport(next.withView(yawOf(heading), 0));
        target.ifPresent(player -> bite(player, ticks));
        record(next);
        body.follow(new ArrayList<>(trail), ticks);
    }

    private void steer(@Nullable SkyBlockPlayer target, int ticks) {
        Vec desired = desiredHeading(target);
        heading = heading.mul(1 - TURN_RATE).add(desired.mul(TURN_RATE)).normalize();
        heading = heading.rotateAroundY(Math.sin(ticks * SNAKE_FREQUENCY) * SNAKE_AMPLITUDE);
    }

    private Vec desiredHeading(@Nullable SkyBlockPlayer target) {
        Pos position = getPosition();
        if (target != null) {
            return target.getPosition().add(0, AIM_HEIGHT, 0).sub(position).asVec().normalize();
        }
        if (home != null && position.distance(home) > LEASH) {
            return home.sub(position).asVec().normalize();
        }
        return heading.withY(0).normalize();
    }

    private void bite(SkyBlockPlayer player, int ticks) {
        if (ticks % BITE_INTERVAL != 0) return;
        if (player.getPosition().add(0, AIM_HEIGHT, 0).distance(getPosition().add(0, 1.5, 0)) > BITE_RANGE) return;
        strike(player, getProfile().damage());
    }

    private void record(Pos position) {
        trail.addFirst(position);
        while (trail.size() > (WormBody.SEGMENTS + 1) * WormBody.SPACING + 1) trail.removeLast();
    }

    private static float yawOf(Vec direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x(), direction.z()));
    }

    @Override
    protected void onDefeated() {
        body.remove();
    }

    @Override
    public void remove() {
        body.remove();
        super.remove();
    }
}
