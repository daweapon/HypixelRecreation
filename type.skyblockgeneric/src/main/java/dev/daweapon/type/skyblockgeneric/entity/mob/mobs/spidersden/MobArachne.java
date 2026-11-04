package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.runecrafting.RunicMobs;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneTier;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneVenom;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobArachne extends SpiderDenMob {
    private static final double HIT_CAP = 1000;
    private static final double[] SWARM_AT = {0.5, 0.2};
    private static final long VENOM_IDLE_MILLIS = 6_000;
    private static final double VENOM_RANGE = 30;
    private static final double SWARM_RADIUS = 5;

    private final ArachneTier tier;
    private final List<MobArachneBrood> brood = new ArrayList<>();
    private int swarms;
    private boolean swarming;
    private long lastStrike = System.currentTimeMillis();

    public MobArachne(ArachneTier tier) {
        super(tier.arachne());
        this.tier = tier;
    }

    public ArachneTier tier() {
        return tier;
    }

    public boolean swarming() {
        return swarming;
    }

    @Override
    protected double hitCap() {
        return HIT_CAP;
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
    public void onSpawn() {
        super.onSpawn();
        rollRunic();
    }

    private void rollRunic() {
        if (isRunic()) return;
        float fraction = getHealth() / getMaxHealth();
        RunicMobs.rollSpawn(this);
        if (isRunic()) setHealth(getMaxHealth() * fraction);
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return !swarming;
    }

    @Override
    public void attack(Entity target, boolean swingHand) {
        if (swarming) return;
        lastStrike = System.currentTimeMillis();
        super.attack(target, swingHand);
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (swarming || swarms >= SWARM_AT.length || isDead()) return;
        if (getHealth() > getMaxHealth() * SWARM_AT[swarms]) return;
        swarms++;
        beginSwarm();
    }

    private void beginSwarm() {
        Instance instance = getInstance();
        if (instance == null) return;
        swarming = true;
        setInvisible(true);
        setTarget(null);
        ArachneFight.say(this, swarms == 1 ? tier.firstSwarm(java.time.LocalTime.now()) : tier.secondSwarm());
        int count = swarms == 1 ? tier.broodFirst() : tier.broodSecond();
        Pos center = getPosition();
        for (int i = 0; i < count; i++) {
            double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
            double radius = 1 + ThreadLocalRandom.current().nextDouble(SWARM_RADIUS);
            MobArachneBrood child = new MobArachneBrood(tier, this);
            brood.add(child);
            child.setInstance(instance, center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        }
    }

    void broodDied() {
        if (!swarming || brood.stream().anyMatch(child -> !child.isDead() && !child.isRemoved())) return;
        brood.clear();
        swarming = false;
        setInvisible(false);
        rollRunic();
        lastStrike = System.currentTimeMillis();
        if (swarms >= SWARM_AT.length && !tier.afterSecondSwarm().isEmpty()) ArachneFight.say(this, tier.afterSecondSwarm());
    }

    public void clearBrood() {
        brood.forEach(child -> {
            if (!child.isRemoved()) child.remove();
        });
        brood.clear();
    }

    @Override
    protected void onAiTick(int ticks) {
        if (swarming) {
            setTarget(null);
            return;
        }
        if (ticks % 20 != 0 || System.currentTimeMillis() - lastStrike < VENOM_IDLE_MILLIS) return;
        rangedTarget(VENOM_RANGE).ifPresent(this::venomShot);
    }

    private void venomShot(SkyBlockPlayer target) {
        lastStrike = System.currentTimeMillis();
        float dealt = MobAttackService.strike(this, target, tier.arachne().damage());
        ArachneVenom.infect(target);
        target.sendMessage("<d>Arachne used <2>Venom Shot <d>on you hitting you for <c>" + Math.round(dealt)
                + " damage <d>and infecting you with venom.");
    }

    @Override
    protected void afterDeath(Instance instance, Pos position) {
        clearBrood();
        ArachneFight.defeated(this);
    }
}
