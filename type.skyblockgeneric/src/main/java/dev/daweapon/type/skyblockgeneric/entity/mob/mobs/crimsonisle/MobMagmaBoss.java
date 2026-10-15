package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.crimson.mob.LavaBeam;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Magma Boss (wiki: Magma_Boss): 160M health in the Magma Chamber. It fires a fireball a second and jumps onto
 * a fire circle, launching 3 fire zones before it lands; after at least 3 jumps, every 32M damage taken splits it
 * into Magma Glares and Unstable Magmas that must all die before it reforms, hitting harder each time. At zero it
 * shrinks to 10M health and flees, firing as it runs. Jump timings, crush and fire-zone damage, the per-split
 * spawn counts (2 Glares and 2 Unstable Magmas per attacker, up to 10 each) and the +20% per reform are
 * estimates.
 */
public class MobMagmaBoss extends CrimsonMinibossMob {
    static final float SPLIT_EVERY = 32_000_000;
    static final float FLEE_HEALTH = 10_000_000;
    static final double REFORM_DAMAGE_BONUS = 0.2;
    private static final double ARENA = 30;
    private static final int JUMP_INTERVAL = 200;
    private static final int JUMP_TELEGRAPH = 40;
    private static final double CRUSH_RADIUS = 6;
    private static final double CRUSH_DAMAGE = 6_000;
    private static final double ZONE_RADIUS = 3;
    private static final double ZONE_DAMAGE = 1_500;
    private static final int MINIONS_PER_ATTACKER = 2;
    private static final int MAX_MINIONS = 10;

    private enum State { NORMAL, SPLIT, FLEE }

    private final List<CrimsonIsleMob> splitMobs = new ArrayList<>();
    private final List<Pos> fireZones = new ArrayList<>();
    private State state = State.NORMAL;
    private float nextSplitAt;
    private int jumpsSinceSplit;
    private int jumpTimer = JUMP_INTERVAL;
    private @Nullable Pos jumpTarget;
    private int telegraph;
    private double damageMultiplier = 1;

    public MobMagmaBoss() {
        super(CrimsonMobs.MAGMA_BOSS);
    }

    @Override
    public CrimsonMiniboss miniboss() {
        return CrimsonMiniboss.MAGMA_BOSS;
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(12));
        nextSplitAt = getMaxHealth() - SPLIT_EVERY;
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return state != State.SPLIT;
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (state == State.NORMAL && damage.getAttacker() instanceof SkyBlockPlayer player && damage.getAmount() >= getHealth()) {
            getDamageDealt().merge(player.getUuid(), (double) getHealth(), Double::sum);
            flee();
            return true;
        }
        return super.damage(damage);
    }

    @Override
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = fighters(ARENA);
        switch (state) {
            case NORMAL -> tickNormal(ticks, fighters);
            case SPLIT -> tickSplit();
            case FLEE -> tickFlee(ticks, fighters);
        }
        tickFireZones(ticks, fighters);
    }

    private double hit() {
        return getProfile().damage() * damageMultiplier;
    }

    private void tickNormal(int ticks, List<SkyBlockPlayer> fighters) {
        if (fighters.isEmpty()) return;
        if (ticks % 20 == 0) {
            fireball(fighters.get(ThreadLocalRandom.current().nextInt(fighters.size())), hit());
        }
        if (jumpTarget != null) {
            LavaBeam.ring(getInstance(), jumpTarget, CRUSH_RADIUS, Particle.FLAME);
            if (--telegraph == 0) land();
            return;
        }
        if (--jumpTimer <= 0) {
            jumpTimer = JUMP_INTERVAL;
            jumpTarget = fighters.get(ThreadLocalRandom.current().nextInt(fighters.size())).getPosition();
            telegraph = JUMP_TELEGRAPH;
            fireZones.clear();
            Pos centre = home();
            for (int i = 0; i < 3; i++) {
                Vec away = jumpTarget.sub(centre).asVec().withY(0);
                if (away.lengthSquared() < 1) away = new Vec(1, 0, 0);
                Vec side = away.normalize().mul(-ARENA * 0.6).rotateAroundY((i - 1) * 0.6);
                fireZones.add(centre.add(side));
            }
            return;
        }
        if (getHealth() <= nextSplitAt && jumpsSinceSplit >= 3 && nextSplitAt > 0) split(fighters);
    }

    private void land() {
        teleport(jumpTarget);
        MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, jumpTarget, 2f, 6);
        Pos landed = jumpTarget;
        fighters(ARENA).stream().filter(player -> player.getPosition().distance(landed) <= CRUSH_RADIUS)
                .forEach(player -> strike(player, CRUSH_DAMAGE * damageMultiplier));
        jumpTarget = null;
        jumpsSinceSplit++;
    }

    private void tickFireZones(int ticks, List<SkyBlockPlayer> fighters) {
        for (Pos zone : fireZones) {
            if (ticks % 5 == 0) LavaBeam.ring(getInstance(), zone, ZONE_RADIUS, Particle.FLAME);
            if (ticks % 10 != 0) continue;
            fighters.stream().filter(player -> player.getPosition().distance(zone) <= ZONE_RADIUS)
                    .forEach(player -> trueStrike(player, ZONE_DAMAGE));
        }
    }

    private void split(List<SkyBlockPlayer> fighters) {
        state = State.SPLIT;
        nextSplitAt -= SPLIT_EVERY;
        jumpsSinceSplit = 0;
        fireZones.clear();
        teleport(home());
        setInvisible(true);
        Map<UUID, SkyBlockPlayer> attackers = new HashMap<>();
        fighters.stream().filter(this::isProvokedBy).forEach(player -> attackers.put(player.getUuid(), player));
        if (attackers.isEmpty()) fighters.forEach(player -> attackers.put(player.getUuid(), player));
        int glares = 0;
        int unstable = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (SkyBlockPlayer player : attackers.values()) {
            for (int i = 0; i < MINIONS_PER_ATTACKER && glares < MAX_MINIONS; i++, glares++) {
                spawnSplit(new MobMagmaGlare(player), random);
            }
            for (int i = 0; i < MINIONS_PER_ATTACKER && unstable < MAX_MINIONS; i++, unstable++) {
                spawnSplit(new MobUnstableMagma(switch (random.nextInt(3)) {
                    case 0 -> CrimsonMobs.UNSTABLE_MAGMA_40;
                    case 1 -> CrimsonMobs.UNSTABLE_MAGMA_60;
                    default -> CrimsonMobs.UNSTABLE_MAGMA_80;
                }).pit(home()), random);
            }
        }
        fighters.forEach(player -> player.sendMessage("<4><l>MAGMA BOSS <c>split apart! Kill the Magmas!"));
    }

    private void spawnSplit(CrimsonIsleMob mob, ThreadLocalRandom random) {
        mob.setInstance(getInstance(), home().add(random.nextDouble(-8, 8), 1, random.nextDouble(-8, 8)));
        splitMobs.add(mob);
    }

    private void tickSplit() {
        splitMobs.removeIf(mob -> mob.isDead() || mob.isRemoved());
        if (!splitMobs.isEmpty()) return;
        state = State.NORMAL;
        setInvisible(false);
        damageMultiplier += REFORM_DAMAGE_BONUS;
        int percent = (int) Math.round((damageMultiplier - 1) * 100);
        fighters(ARENA).forEach(player -> player.sendMessage(
                "<4><l>MAGMA BOSS <c>reformed! Its damage is now increased by <e>" + percent + "%<c>!"));
    }

    private void flee() {
        state = State.FLEE;
        fireZones.clear();
        jumpTarget = null;
        setMaxHealth(FLEE_HEALTH);
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(6));
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3);
        refreshName();
    }

    private void tickFlee(int ticks, List<SkyBlockPlayer> fighters) {
        if (getHealth() < FLEE_HEALTH / 2) editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(4));
        nearestPlayer(ARENA, player -> true).ifPresent(player -> {
            Vec away = getPosition().sub(player.getPosition()).asVec().withY(0);
            if (away.lengthSquared() < 0.01) away = new Vec(1, 0, 0);
            double speed = 6 + 6 * (1 - getHealth() / FLEE_HEALTH);
            setVelocity(away.normalize().mul(speed).withY(getVelocity().y()));
            if (ticks % 30 == 0) fireball(player, hit());
        });
    }

    @Override
    public void remove() {
        splitMobs.forEach(CrimsonIsleMob::remove);
        super.remove();
    }
}
