package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;

final class ClickHopSpecial implements CreatureSpecial {
    private static final double HOP_SPEED = 9;
    private static final double HOP_LIFT = 6.5;
    private static final int HOP_TICKS = 25;
    private static final long HOP_MILLIS = HOP_TICKS * 50L;

    private final int hopLimit;
    private final PerMob<State> states = new PerMob<>();

    private static final class State {
        final HopCounter counter;
        long busyUntil;

        State(int limit) {
            this.counter = new HopCounter(limit);
        }
    }

    ClickHopSpecial(int hopLimit) {
        this.hopLimit = hopLimit;
    }

    @Override
    public boolean onInteract(SkyBlockPlayer player, HuntingCreatureMob mob) {
        State state = states.of(mob, () -> new State(hopLimit));
        long now = System.currentTimeMillis();
        if (state.counter.exhausted() || now < state.busyUntil) return true;
        state.busyUntil = now + HOP_MILLIS;
        boolean exhausted = state.counter.hop();
        Vec away = CreatureMotion.horizontal(player.getPosition(), mob.getPosition());
        mob.setVelocity(new Vec(away.x() * HOP_SPEED, HOP_LIFT, away.z() * HOP_SPEED));
        mob.setGlowing(true);
        SpecialEffects.burst(mob, Particle.HAPPY_VILLAGER, mob.getPosition().add(0, 0.3, 0), 12, 0.3f);
        SpecialEffects.later(HOP_TICKS, () -> land(player, mob, exhausted));
        return true;
    }

    private void land(SkyBlockPlayer player, HuntingCreatureMob mob, boolean exhausted) {
        if (mob.isRemoved() || mob.isDead()) {
            states.clear(mob);
            return;
        }
        mob.setGlowing(false);
        if (!exhausted) return;
        SpecialMessages.send(player, mob.spec(), "exhausted");
        states.clear(mob);
        mob.capture(player);
    }
}
