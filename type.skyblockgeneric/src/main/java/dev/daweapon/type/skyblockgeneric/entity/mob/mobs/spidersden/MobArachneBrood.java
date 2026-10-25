package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.entity.Entity;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneTier;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneVenom;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;

public class MobArachneBrood extends SpiderDenMob {
    private static final long VENOM_IDLE_MILLIS = 6_000;
    private static final double VENOM_RANGE = 20;

    private final ArachneTier tier;
    private final MobArachne mother;
    private long lastStrike = System.currentTimeMillis();

    public MobArachneBrood(ArachneTier tier, MobArachne mother) {
        super(tier.brood());
        this.tier = tier;
        this.mother = mother;
    }

    @Override
    protected double hitCap() {
        return tier.broodHitCap();
    }

    @Override
    protected boolean immuneToMagic() {
        return true;
    }

    @Override
    public boolean isImmuneToArrows() {
        return true;
    }

    @Override
    protected float aggroRange() {
        return 40;
    }

    @Override
    public void attack(Entity target, boolean swingHand) {
        lastStrike = System.currentTimeMillis();
        super.attack(target, swingHand);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 20 != 0 || System.currentTimeMillis() - lastStrike < VENOM_IDLE_MILLIS) return;
        rangedTarget(VENOM_RANGE).ifPresent(target -> {
            lastStrike = System.currentTimeMillis();
            float dealt = MobAttackService.strike(this, target, tier.brood().damage());
            ArachneVenom.infect(target);
            target.sendMessage("<d>" + getProfile().displayName() + " used <2>Venom Shot <d>on you hitting you for <c>" + Math.round(dealt)
                    + " damage <d>and infecting you with venom.");
        });
    }

    @Override
    protected void afterDeath(Instance instance, Pos position) {
        mother.broodDied();
    }
}
