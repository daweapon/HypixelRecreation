package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

final class StandStillSpecial implements CreatureSpecial {
    private static final double MOVE_EPSILON_SQUARED = 0.0025;

    private final double radius;
    private final long delayTicks;
    private final long yellowTicks;
    private final long readyTicks;
    private final Map<HuntingCreatureMob, State> states = Collections.synchronizedMap(new WeakHashMap<>());

    private static final class State {
        final StandStillTimer timer;
        final Map<UUID, Pos> lastSeen = new HashMap<>();
        StandStillTimer.Phase shown = StandStillTimer.Phase.IDLE;

        State(StandStillTimer timer) {
            this.timer = timer;
        }
    }

    StandStillSpecial(double radius, long delayTicks, long yellowTicks, long readyTicks) {
        this.radius = radius;
        this.delayTicks = delayTicks;
        this.yellowTicks = yellowTicks;
        this.readyTicks = readyTicks;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (mob.getInstance() == null) return;
        State state = state(mob);
        boolean nearby = false;
        boolean moved = false;
        Map<UUID, Pos> seen = new HashMap<>();
        for (var entity : mob.getInstance().getNearbyEntities(mob.getPosition(), radius)) {
            if (!(entity instanceof SkyBlockPlayer player) || player.isDead()) continue;
            nearby = true;
            Pos previous = state.lastSeen.get(player.getUuid());
            if (previous == null || previous.distanceSquared(player.getPosition()) > MOVE_EPSILON_SQUARED) moved = true;
            seen.put(player.getUuid(), player.getPosition());
        }
        state.lastSeen.clear();
        state.lastSeen.putAll(seen);
        StandStillTimer.Phase phase = state.timer.tick(nearby, moved);
        if (phase != state.shown) {
            state.shown = phase;
            mob.setHeader(switch (phase) {
                case RED -> "<c><l>⚠";
                case YELLOW -> "<e><l>⚠";
                default -> "";
            });
        }
    }

    private boolean ready(HuntingCreatureMob mob) {
        State state = states.get(mob);
        return state != null && state.timer.phase() == StandStillTimer.Phase.READY && state.shown == StandStillTimer.Phase.READY;
    }

    private State state(HuntingCreatureMob mob) {
        return states.computeIfAbsent(mob, key -> new State(new StandStillTimer(delayTicks, yellowTicks, readyTicks)));
    }

    private void scare(SkyBlockPlayer player, HuntingCreatureMob mob) {
        SpecialMessages.send(player, mob.spec(), "too_early");
        mob.remove();
    }

    @Override
    public boolean allowLasso(SkyBlockPlayer player, HuntingCreatureMob mob) {
        if (ready(mob)) return true;
        scare(player, mob);
        return false;
    }

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        if (ready(mob)) return false;
        scare(player, mob);
        return true;
    }
}
