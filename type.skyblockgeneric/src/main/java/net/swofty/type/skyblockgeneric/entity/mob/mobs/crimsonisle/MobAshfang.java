package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.AshfangArena;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.AshfangMinionMob;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ashfang, "The Twins" (wiki: Ashfang): immobile behind a Scorched Barrier that only Blazing Souls get through
 * (2,000,000 each). Once attacked, the barrier burns players inside the flame ring for 40% of their max health a
 * second. A 50 s summon phase raises 4 Followers, 4 Acolytes and 4 Underlings, Gravity Orbs from 15 s that pull
 * in players and Followers (a Follower held 5 s explodes), and Noxious Ash Clouds; a 10 s healing phase absorbs
 * the survivors for 250,000 each. The ring radius, orb pull range and cloud cadence are estimates.
 */
public class MobAshfang extends CrimsonMinibossMob implements AshfangArena {
    static final double SOUL_DAMAGE = 2_000_000;
    static final double HEAL_PER_MINION = 250_000;
    private static final double BARRIER_RADIUS = 5;
    private static final double BARRIER_SHARE = 0.4;
    private static final int SUMMON_TICKS = 1000;
    private static final int HEAL_TICKS = 200;
    private static final int ORBS_AT = 300;
    private static final double ORB_PULL_RANGE = 6;
    private static final double ORB_TOUCH = 1.5;
    private static final double ORB_DAMAGE = 5_000;
    private static final int FOLLOWER_EXPLODES_AFTER = 100;
    private static final double CLOUD_RADIUS = 3;
    private static final double CLOUD_DAMAGE = 5_000;
    private static final int CLOUD_TICKS = 200;
    private static final int IDLE_RESET_TICKS = 600;

    private final List<AshfangMinionMob> minions = new ArrayList<>();
    private final List<Pos> orbs = new ArrayList<>();
    private final Map<AshfangMinionMob, Integer> heldFollowers = new HashMap<>();
    private final Map<Pos, Integer> clouds = new HashMap<>();
    private boolean fighting;
    private boolean soulStrike;
    private boolean healing;
    private int phaseTicks;
    private int idleTicks;

    public MobAshfang() {
        super(CrimsonMobs.ASHFANG);
    }

    @Override
    public CrimsonMiniboss miniboss() {
        return CrimsonMiniboss.ASHFANG;
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(2.5);
        setNoGravity(true);
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        if (!fighting) startFight();
        return soulStrike;
    }

    @Override
    public boolean soulHit(SkyBlockPlayer player, Pos soul) {
        if (soul.distance(getPosition().add(0, 1.5, 0)) > 2.5) return false;
        soulStrike = true;
        damage(new Damage(DamageType.MAGIC, player, player, player.getPosition(), (float) SOUL_DAMAGE));
        soulStrike = false;
        return true;
    }

    @Override
    public void dropSoul(Pos at) {
        if (getInstance() == null || isDead()) return;
        new MobBlazingSoul(this).setInstance(getInstance(), at);
    }

    private void startFight() {
        fighting = true;
        healing = false;
        phaseTicks = SUMMON_TICKS;
        summon();
    }

    private void summon() {
        Pos centre = miniboss().getSpawn();
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12;
            AshfangMinionMob minion = switch (i % 3) {
                case 0 -> new MobAshfangFollower();
                case 1 -> new MobAshfangAcolyte();
                default -> new MobAshfangUnderling();
            };
            minion.bind(this);
            minion.setInstance(getInstance(), centre.add(Math.cos(angle) * 7, 0, Math.sin(angle) * 7));
            minions.add(minion);
        }
    }

    @Override
    protected void onAiTick(int ticks) {
        if (!fighting) return;
        minions.removeIf(minion -> minion.isDead() || minion.isRemoved());
        List<SkyBlockPlayer> nearby = fighters(30);
        idleTicks = nearby.isEmpty() ? idleTicks + 1 : 0;
        if (idleTicks >= IDLE_RESET_TICKS) {
            reset();
            return;
        }
        if (ticks % 20 == 0) burnBarrier(nearby);
        tickOrbs(nearby);
        tickClouds(nearby, ticks);
        if (--phaseTicks > 0) {
            if (!healing && SUMMON_TICKS - phaseTicks >= ORBS_AT && orbs.size() < 3 && (SUMMON_TICKS - phaseTicks - ORBS_AT) % 20 == 0) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                orbs.add(miniboss().getSpawn().add(random.nextDouble(-7, 7), 1.5, random.nextDouble(-7, 7)));
            }
            return;
        }
        if (healing) {
            healing = false;
            phaseTicks = SUMMON_TICKS;
            summon();
        } else {
            healing = true;
            phaseTicks = HEAL_TICKS;
            orbs.clear();
            heldFollowers.clear();
            float heal = (float) (HEAL_PER_MINION * minions.size());
            minions.forEach(AshfangMinionMob::absorb);
            minions.clear();
            setHealth(Math.min(getMaxHealth(), getHealth() + heal));
            refreshName();
        }
    }

    private void burnBarrier(List<SkyBlockPlayer> nearby) {
        LavaBeam.ring(getInstance(), miniboss().getSpawn(), BARRIER_RADIUS, Particle.FLAME);
        nearby.stream().filter(player -> player.getPosition().distance(getPosition()) <= BARRIER_RADIUS)
                .forEach(player -> trueStrike(player, player.getMaxHealth() * BARRIER_SHARE));
    }

    private void tickOrbs(List<SkyBlockPlayer> nearby) {
        for (Pos orb : orbs) {
            MythologicalCombat.particles(getInstance(), Particle.REVERSE_PORTAL, orb, 0.6f, 8);
            for (SkyBlockPlayer player : nearby) {
                Vec pull = orb.sub(player.getPosition()).asVec();
                if (pull.length() <= ORB_TOUCH) {
                    if (getAliveTicks() % 20 == 0) trueStrike(player, ORB_DAMAGE);
                } else if (pull.length() <= ORB_PULL_RANGE) {
                    player.setVelocity(pull.normalize().mul(6));
                }
            }
            for (AshfangMinionMob minion : minions) {
                if (!(minion instanceof MobAshfangFollower)) continue;
                Vec pull = orb.sub(minion.getPosition()).asVec();
                if (pull.length() <= ORB_TOUCH) {
                    heldFollowers.merge(minion, 1, Integer::sum);
                } else if (pull.length() <= ORB_PULL_RANGE) {
                    minion.setVelocity(pull.normalize().mul(6));
                }
            }
        }
        heldFollowers.entrySet().removeIf(entry -> {
            if (entry.getValue() < FOLLOWER_EXPLODES_AFTER) return false;
            MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, entry.getKey().getPosition(), 1f, 3);
            entry.getKey().collapse();
            return true;
        });
    }

    private void tickClouds(List<SkyBlockPlayer> nearby, int ticks) {
        if (!healing && ticks % CLOUD_TICKS == 0 && !nearby.isEmpty()) {
            SkyBlockPlayer target = nearby.get(ThreadLocalRandom.current().nextInt(nearby.size()));
            clouds.put(target.getPosition(), 100);
        }
        clouds.replaceAll((pos, left) -> left - 1);
        clouds.entrySet().removeIf(entry -> entry.getValue() <= 0);
        for (Pos cloud : clouds.keySet()) {
            MythologicalCombat.particles(getInstance(), Particle.LARGE_SMOKE, cloud.add(0, 0.5, 0), 1.5f, 6);
            if (ticks % 20 != 0) continue;
            nearby.stream().filter(player -> player.getPosition().distance(cloud) <= CLOUD_RADIUS)
                    .forEach(player -> trueStrike(player, CLOUD_DAMAGE));
        }
    }

    private void reset() {
        fighting = false;
        healing = false;
        orbs.clear();
        clouds.clear();
        heldFollowers.clear();
        minions.forEach(AshfangMinionMob::absorb);
        minions.clear();
        getDamageDealt().clear();
        heal();
        refreshName();
    }

    @Override
    public void remove() {
        minions.forEach(AshfangMinionMob::absorb);
        minions.clear();
        super.remove();
    }
}
