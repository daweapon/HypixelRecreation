package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.Optional;

/**
 * The wither Boss Corleone calls down (wiki: Smog): it hovers, shoots wither skulls at the nearest player, then
 * explodes after a few seconds and despawns, hurting nearby players. The fire rate, range, lifetime, blast radius
 * and blast damage are estimates.
 */
public class MobSmog extends CrimsonIsleMob {
    private static final double RANGE = 24;
    private static final int FIRE_INTERVAL = 30;
    private static final int LIFETIME_TICKS = 120;
    private static final double BLAST_RADIUS = 6;
    private static final double BLAST_DAMAGE_MULTIPLIER = 2;
    private static final double SKULL_SPEED = 0.7;

    public MobSmog() {
        super(CrystalHollowsBossProfiles.SMOG);
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public boolean countsForBestiary() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks >= LIFETIME_TICKS) {
            explode();
            return;
        }
        if (ticks % FIRE_INTERVAL != 0) return;
        Optional<SkyBlockPlayer> target = rangedTarget(RANGE);
        target.ifPresent(player -> MobProjectile.witherSkull(this, player, SKULL_SPEED,
                hit -> MobAttackService.strike(this, hit, getProfile().damage())));
    }

    private void explode() {
        MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, getPosition().add(0, 1, 0), 2f, 12);
        MythologicalCombat.sound(this, "entity.generic.explode", 1.5f, 0.9f);
        for (SkyBlockPlayer player : playersWithin(BLAST_RADIUS)) {
            strike(player, getProfile().damage() * BLAST_DAMAGE_MULTIPLIER);
        }
        remove();
    }
}
