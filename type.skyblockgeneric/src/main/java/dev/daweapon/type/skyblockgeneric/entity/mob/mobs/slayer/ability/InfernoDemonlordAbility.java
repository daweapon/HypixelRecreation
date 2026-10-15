package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.DaggerVengeance;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.HellionAttunement;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.HellionShield;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * The Inferno Demonlord, after the wiki's Blaze Slayer pages: Immolate burns everyone in range for
 * flat true damage by health band; from Tier II the Hellion Shield only lets matching dagger hits
 * through; at its split thresholds it becomes Quazii and Typhoeus; Fire Pillars must be broken in
 * time; and from a third of its health at Tier III+ the DDR Apocalypse spawns fire pits.
 */
final class InfernoDemonlordAbility extends SlayerAbilitySupport {
    static final double FIGHT_RANGE = 16;
    private static final double[][] IMMOLATE = {{45, 65, 115}, {65, 130, 190}, {80, 160, 320}, {385, 575, 955}};
    private static final double[] PILLAR_FROM = {0, 0.5, 2D / 3D, 2D / 3D};
    private static final double[][] SPLITS = {{0.5}, {0.5}, {2D / 3D, 1D / 3D}, {2D / 3D, 1D / 3D}};
    private static final double APOCALYPSE_FROM = 1D / 3D;

    private SlayerTier tier;
    private HellionShield shield;
    private final Deque<Double> pendingSplits = new ArrayDeque<>();
    private InfernoDemons demons;
    private InfernoFirePits pits;
    private boolean pillarsStarted;
    private boolean apocalypseStarted;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        tier = boss.getProfile().tier().tier();
        int index = index();
        for (double threshold : SPLITS[index]) pendingSplits.add(threshold);
        pits = new InfernoFirePits(this, boss, tier);
        if (tier.number() >= 2) {
            shield = new HellionShield(List.of(HellionAttunement.values()), () -> {
                announce(boss, "<6>The Hellion Shield attuned to " + shield.current().coloured() + "<6>!");
                boss.refreshName();
            });
        }
        repeating(boss, 20, 20, () -> {
            if (demons != null && demons.active()) return;
            double damage = IMMOLATE[index][band(boss)];
            nearbyPlayers(boss, FIGHT_RANGE).forEach(player -> trueDamage(boss, player, damage, "Immolate", false));
            particles(boss, Particle.FLAME, 1.3f, 35);
        });
        boss.refreshName();
    }

    @Override
    public float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount) {
        if (demons != null && demons.active()) return 0F;
        if (shield == null || DaggerVengeance.isApplying()) return amount;
        float filtered = shield.filter(boss, damage, amount);
        boss.refreshName();
        return filtered;
    }

    @Override
    public void onDamaged(SlayerBossMob boss, Damage damage, float appliedDamage) {
        double ratio = healthRatio(boss);
        if (!pillarsStarted && tier.number() >= 2 && ratio <= PILLAR_FROM[index()]) {
            pillarsStarted = true;
            InfernoFirePillar.startCycle(this, boss);
        }
        if (!apocalypseStarted && tier.number() >= 3 && ratio <= APOCALYPSE_FROM) {
            apocalypseStarted = true;
            announce(boss, "<c><l>DDR APOCALYPSE!");
            pits.startApocalypse();
        }
        if (!pendingSplits.isEmpty() && ratio <= pendingSplits.peekFirst() && (demons == null || !demons.active())) {
            pendingSplits.removeFirst();
            split(boss);
        }
    }

    private void split(SlayerBossMob boss) {
        if (tier.number() >= 3) pits.burst();
        boss.setInvisible(true);
        announce(boss, "<6><l>DEMONSPLIT! <e>Quazii and Typhoeus emerge from the Demonlord.");
        demons = new InfernoDemons(this, boss, tier, () -> {
            boss.setInvisible(false);
            announce(boss, "<c>The Inferno Demonlord reforms!");
            if (tier.number() >= 3) pits.burst();
            boss.refreshName();
        });
        demons.spawn();
        boss.refreshName();
    }

    @Override
    public boolean canAttack(SlayerBossMob boss) {
        return demons == null || !demons.active();
    }

    @Override
    public String nameHeader(SlayerBossMob boss) {
        if (demons != null && demons.active()) return "<7>Split into demons";
        return shield == null ? "" : shield.header();
    }

    @Override
    public void onDeath(SlayerBossMob boss) {
        if (demons != null) demons.despawn();
        pits.clear();
    }

    private int band(SlayerBossMob boss) {
        double ratio = healthRatio(boss);
        return ratio > 2D / 3D ? 0 : ratio > 1D / 3D ? 1 : 2;
    }

    private int index() {
        return Math.min(3, tier.number() - 1);
    }

    void announce(SlayerBossMob boss, String message) {
        nearbyPlayers(boss, FIGHT_RANGE * 2).forEach(player -> player.sendMessage(message));
    }

    List<SkyBlockPlayer> fighters(SlayerBossMob boss) {
        return nearbyPlayers(boss, FIGHT_RANGE * 2);
    }

    void hurt(SlayerBossMob boss, SkyBlockPlayer player, double damage, String source, boolean chat) {
        trueDamage(boss, player, damage, source, chat);
    }

    void schedule(SlayerBossMob boss, int delayTicks, Runnable runnable) {
        delayed(boss, delayTicks, runnable);
    }

    void every(SlayerBossMob boss, int delayTicks, int periodTicks, Runnable runnable) {
        repeating(boss, delayTicks, periodTicks, runnable);
    }

    SkyBlockPlayer ownerOf(SlayerBossMob boss) {
        return owner(boss);
    }
}
