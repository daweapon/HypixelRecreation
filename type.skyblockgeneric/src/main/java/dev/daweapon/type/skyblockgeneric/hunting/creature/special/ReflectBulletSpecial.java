package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;

import java.util.Optional;

final class ReflectBulletSpecial implements CreatureSpecial {
    private static final double TOP_REACH = 0.9;
    private static final double MIN_RISE = 0.5;
    private static final double MAX_RISE = 2.0;
    private static final int SHOT_COOLDOWN_TICKS = 60;
    private static final byte OPEN = 100;

    private final int returnsNeeded;
    private final PerMob<State> states = new PerMob<>();

    private static final class State {
        final BulletExchange exchange;
        boolean bulletFlying;
        long nextShot;

        State(int needed) {
            this.exchange = new BulletExchange(needed);
        }
    }

    ReflectBulletSpecial(int returnsNeeded) {
        this.returnsNeeded = returnsNeeded;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (mob.getInstance() == null) return;
        State state = states.of(mob, () -> new State(returnsNeeded));
        Optional<SkyBlockPlayer> rider = standingOn(mob);
        mob.editEntityMeta(ShulkerMeta.class, meta -> meta.setShieldHeight(rider.isPresent() || state.bulletFlying ? OPEN : 0));
        if (rider.isEmpty() || state.bulletFlying || ticks < state.nextShot) return;
        state.bulletFlying = true;
        state.nextShot = ticks + SHOT_COOLDOWN_TICKS;
        ReflectBullet.launch(mob, rider.get(), state.exchange, () -> state.bulletFlying = false);
    }

    private static Optional<SkyBlockPlayer> standingOn(HuntingCreatureMob mob) {
        for (Entity entity : mob.getInstance().getNearbyEntities(mob.getPosition(), 2.5)) {
            if (!(entity instanceof SkyBlockPlayer player) || player.isDead()) continue;
            double rise = player.getPosition().y() - mob.getPosition().y();
            if (Math.abs(player.getPosition().x() - mob.getPosition().x()) > TOP_REACH) continue;
            if (Math.abs(player.getPosition().z() - mob.getPosition().z()) > TOP_REACH) continue;
            if (rise >= MIN_RISE && rise <= MAX_RISE) return Optional.of(player);
        }
        return Optional.empty();
    }
}
