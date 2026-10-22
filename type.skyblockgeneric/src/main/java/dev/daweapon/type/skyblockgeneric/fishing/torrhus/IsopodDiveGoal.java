package dev.daweapon.type.skyblockgeneric.fishing.torrhus;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;

public final class IsopodDiveGoal extends GoalSelector {
    private final SkyBlockMob mob;
    private boolean dived;
    private boolean diving;
    private boolean gravityWas;
    private int waypoint;

    public IsopodDiveGoal(EntityCreature creature) {
        super(creature);
        this.mob = (SkyBlockMob) creature;
    }

    @Override
    public boolean shouldStart() {
        return IsopodDivePath.shouldDive(mob.getHealth(), mob.getMaxHealth(), dived);
    }

    @Override
    public void start() {
        dived = true;
        diving = true;
        waypoint = 0;
        gravityWas = mob.hasNoGravity();
        mob.getVanillaNavigator().stop();
        mob.setInvulnerable(true);
        mob.setNoGravity(true);
    }

    @Override
    public void tick(long time) {
        if (!diving) return;
        Pos target = IsopodDivePath.WAYPOINTS.get(waypoint);
        Pos next = IsopodDivePath.step(mob.getPosition(), target, IsopodDivePath.BLOCKS_PER_TICK);
        mob.teleport(next);
        waypoint = IsopodDivePath.nextWaypoint(next, waypoint);
        if (IsopodDivePath.finished(waypoint)) diving = false;
    }

    @Override
    public boolean shouldEnd() {
        return !diving;
    }

    @Override
    public void end() {
        mob.setInvulnerable(false);
        mob.setNoGravity(gravityWas);
    }
}
