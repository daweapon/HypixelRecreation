package dev.daweapon.type.kuudra.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import dev.daweapon.type.kuudra.fight.KuudraFight;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Base for Kuudra's minions: stats per tier from {@code mobs.yml}, Kuudra Bestiary credit, run-wide perks on the
 * damage they take, tokens for the party when they die, and Freezing Touch.
 */
public abstract class KuudraMob extends CrimsonIsleMob {
    private static final String KUUDRA_HEAD = "5051c83d9ebf69013f1ec8c9efc979ec2d925a921cc877ff64abe09aadd2f6cc";
    private static final ThreadLocal<Boolean> SPREADING = ThreadLocal.withInitial(() -> false);

    protected final KuudraFight fight;
    private final KuudraMobSpec spec;
    private long frozenUntil;

    protected KuudraMob(KuudraFight fight, KuudraMobSpec spec, EntityType type, MobType... types) {
        super(profile(fight, spec, type, types));
        this.fight = fight;
        this.spec = spec;
    }

    private static CrimsonMobProfile profile(KuudraFight fight, KuudraMobSpec spec, EntityType type, MobType... types) {
        KuudraMobSpec.Stats stats = spec.stats(fight.tier());
        if (stats == null) stats = spec.stats().values().iterator().next();
        CrimsonMobProfile.Builder builder = CrimsonMobProfile.builder(spec.id(), spec.name())
                .level(stats.level())
                .entity(type)
                .stats(stats.health(), stats.damage())
                .speed(fight.spec().mobs().speed(spec.id()))
                .rewards(stats.combatXP(), stats.coins(), stats.xpOrbs())
                .types(types)
                .neverCorrupted();
        if (spec.inBestiary()) builder.bestiary(spec.bestiaryBracket(), maxTier(spec.id()), KUUDRA_HEAD);
        return builder.build();
    }

    private static int maxTier(String mobId) {
        return Arrays.stream(BestiaryCategory.KUUDRA.values())
                .map(family -> family.getMobs().getFirst())
                .filter(mob -> mob.getMobID().equals(mobId))
                .mapToInt(BestiaryMob::getMaxBestiaryTier)
                .findFirst().orElse(0);
    }

    @Override
    public boolean isKuudraForce() {
        return true;
    }

    public KuudraMobSpec spec() {
        return spec;
    }

    public int tokens() {
        return spec.tokens();
    }

    /** Humanoid mobs that break the Ballista while it is being built (research K1 3.2). */
    public boolean attacksBallista() {
        return false;
    }

    @Override
    public boolean countsForBestiary() {
        return BestiaryCategory.KUUDRA.mobIds().contains(getMobID());
    }

    @Override
    protected float aggroRange() {
        return 64;
    }

    @Override
    protected int strollRadius() {
        return 3;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return fight.isTargetable(player) && !fight.isInStomach(player);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return super.damage(damage);
        damage.setAmount((float) (damage.getAmount() * fight.outgoingMultiplier(player, this)));
        float dealt = damage.getAmount();
        boolean hurt = super.damage(damage);
        if (hurt && !SPREADING.get()) {
            SPREADING.set(true);
            try {
                fight.onMobHit(player, this, dealt);
            } finally {
                SPREADING.set(false);
            }
        }
        return hurt;
    }

    @Override
    public void attack(@NotNull Entity target, boolean swingHand) {
        if (isFrozen()) return;
        super.attack(target, swingHand);
    }

    @Override
    protected final void onAiTick(int ticks) {
        if (isFrozen()) {
            setVelocity(Vec.ZERO);
            return;
        }
        behave(ticks);
    }

    protected void behave(int ticks) {
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        fight.onMobKilled(this, killer);
    }

    @Override
    public void kill() {
        if (isDead()) return;
        super.kill();
        onDeath();
        fight.forget(this);
    }

    protected void onDeath() {
    }

    @Override
    public void remove() {
        super.remove();
        fight.forget(this);
    }

    public void freeze(int seconds) {
        frozenUntil = System.currentTimeMillis() + seconds * 1000L;
        setVelocity(Vec.ZERO);
    }

    public boolean isFrozen() {
        return System.currentTimeMillis() < frozenUntil;
    }

    protected Optional<SkyBlockPlayer> nearestTarget(double range) {
        return fight.alivePlayers().stream()
                .filter(player -> player.getInstance() == getInstance())
                .filter(player -> player.getPosition().distance(getPosition()) <= range)
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())));
    }

    protected double damageStat() {
        return getProfile().damage();
    }
}
