package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.particle.Particle;

import java.util.concurrent.ThreadLocalRandom;

final class TotemPuzzleSpecial implements CreatureSpecial {
    private static final double SLEEPING_SCALE = 1.1;
    private static final double DRIFT_LIMIT_SQUARED = 0.16;
    private static final long CLICK_COOLDOWN_MILLIS = 150;
    private static final double CHASE_RANGE = 24;
    private static final double CHASE_SPEED = 0.22;
    private static final double STRIKE_REACH = 2.4;
    private static final long STRIKE_COOLDOWN_MILLIS = 1000;
    private static final int SMOKE_INTERVAL = 4;

    private final PerMob<State> states = new PerMob<>();

    private static final class State {
        final TotemPuzzle puzzle = TotemPuzzle.random(ThreadLocalRandom.current());
        TotemHeads heads;
        Pos home;
        boolean awake;
        long lastClick;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (mob.getInstance() == null) return;
        State state = states.of(mob, State::new);
        if (state.awake) {
            chase(mob, ticks);
            return;
        }
        if (state.heads == null) buildTotem(mob, state);
        hold(mob, state);
    }

    @Override
    public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        State state = states.of(mob, State::new);
        if (state.awake) return false;
        if (state.heads == null) return true;
        click(player, mob, state, true);
        return true;
    }

    @Override
    public boolean onInteract(SkyBlockPlayer player, HuntingCreatureMob mob) {
        State state = states.of(mob, State::new);
        if (state.awake || state.heads == null) return false;
        click(player, mob, state, false);
        return true;
    }

    private void buildTotem(HuntingCreatureMob mob, State state) {
        state.home = mob.getPosition();
        mob.setInvisible(true);
        mob.getAttribute(Attribute.SCALE).setBaseValue(SLEEPING_SCALE);
        state.heads = new TotemHeads(mob, state.puzzle);
    }

    private void hold(HuntingCreatureMob mob, State state) {
        mob.getVanillaNavigator().stop();
        mob.setVelocity(new Vec(0, mob.getVelocity().y(), 0));
        if (mob.getPosition().distanceSquared(state.home) > DRIFT_LIMIT_SQUARED) mob.teleport(state.home);
    }

    private void click(SkyBlockPlayer player, HuntingCreatureMob mob, State state, boolean clockwise) {
        long now = System.currentTimeMillis();
        if (now - state.lastClick < CLICK_COOLDOWN_MILLIS) return;
        state.lastClick = now;
        if (clockwise) SpecialMessages.send(player, mob.spec(), "hit_sleeping");
        state.puzzle.click(clickedHead(player, mob), clockwise);
        state.heads.show(state.puzzle);
        if (state.puzzle.awake()) awaken(player, mob, state);
    }

    private int clickedHead(SkyBlockPlayer player, HuntingCreatureMob mob) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        double flat = Math.hypot(look.x(), look.z());
        if (flat < 1e-6) return TotemPuzzle.HEADS - 1;
        double distance = Math.hypot(mob.getPosition().x() - eye.x(), mob.getPosition().z() - eye.z());
        double height = eye.y() + look.y() / flat * distance - mob.getPosition().y();
        return TotemPuzzle.headAt(height, TotemHeads.HEAD_HEIGHT);
    }

    private void awaken(SkyBlockPlayer player, HuntingCreatureMob mob, State state) {
        state.awake = true;
        state.heads.remove();
        mob.setInvisible(false);
        mob.getAttribute(Attribute.SCALE).setBaseValue(1);
        SpecialMessages.send(player, mob.spec(), "awaken");
    }

    private void chase(HuntingCreatureMob mob, long ticks) {
        if (ticks % SMOKE_INTERVAL == 0) {
            SpecialEffects.burst(mob, Particle.LARGE_SMOKE, mob.getPosition().add(0, 2.8, 0), 3, 0.2f);
        }
        CreatureMotion.nearestPlayer(mob, CHASE_RANGE).ifPresent(player -> {
            if (mob.getPosition().distance(player.getPosition()) <= STRIKE_REACH) {
                CreatureMotion.strike(mob, player, STRIKE_COOLDOWN_MILLIS);
                return;
            }
            CreatureMotion.walk(mob, player.getPosition(), CHASE_SPEED);
        });
    }
}
