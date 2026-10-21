package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import net.minestom.server.particle.Particle;

import java.util.concurrent.atomic.AtomicBoolean;

final class SelfHealSpecial implements CreatureSpecial {
    private final double triggerFraction;
    private final double healFraction;
    private final PerMob<AtomicBoolean> used = new PerMob<>();

    SelfHealSpecial(double triggerFraction, double healFraction) {
        this.triggerFraction = triggerFraction;
        this.healFraction = healFraction;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        AtomicBoolean healed = used.of(mob, AtomicBoolean::new);
        if (healed.get() || mob.getHealth() > mob.getMaxHealth() * triggerFraction) return;
        if (!healed.compareAndSet(false, true)) return;
        mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + (float) (mob.getMaxHealth() * healFraction)));
        mob.refreshName();
        SpecialEffects.burst(mob, Particle.HEART, mob.getPosition().add(0, 1.5, 0), 8, 0.4f);
    }
}
