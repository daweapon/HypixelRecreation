package dev.daweapon.type.skyblockgeneric.hunting.creature;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;

import java.util.concurrent.ThreadLocalRandom;

public class FlyWanderGoal extends GoalSelector {
    private static final double RADIUS = 6;
    private static final double SPEED = 0.12;
    private static final long RETARGET_MS = 3000;

    private Pos home;
    private Point target;
    private long nextTarget;

    public FlyWanderGoal(EntityCreature entity) {
        super(entity);
    }

    @Override
    public boolean shouldStart() {
        return entityCreature.getInstance() != null;
    }

    @Override
    public void start() {
        home = entityCreature.getPosition();
        entityCreature.setNoGravity(true);
        pick();
    }

    private void pick() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        target = home.add(random.nextDouble(-RADIUS, RADIUS), random.nextDouble(0, 3), random.nextDouble(-RADIUS, RADIUS));
        nextTarget = System.currentTimeMillis() + RETARGET_MS;
    }

    @Override
    public void tick(long time) {
        if (System.currentTimeMillis() > nextTarget || entityCreature.getPosition().distanceSquared(target) < 1) pick();
        Vec direction = Vec.fromPoint(target.sub(entityCreature.getPosition()));
        if (direction.lengthSquared() < 1e-6) return;
        entityCreature.setVelocity(direction.normalize().mul(SPEED * 20));
        entityCreature.lookAt(target);
    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void end() {
    }
}
