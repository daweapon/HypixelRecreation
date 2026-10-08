package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.damage.Damage;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.AshfangMinionMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ashfang Acolyte (wiki: Ashfang_Acolyte): stationary, only hurt by melee, fires 6 fireballs every 2 s and
 * teleports away after being attacked for a few seconds. Melee means within 5 blocks (estimate).
 */
public class MobAshfangAcolyte extends AshfangMinionMob {
    static final double MELEE_RANGE = 5;
    private static final int TELEPORT_AFTER = 60;
    private int underAttack;

    public MobAshfangAcolyte() {
        this(CrimsonMobs.ASHFANG_ACOLYTE);
    }

    protected MobAshfangAcolyte(CrimsonMobProfile profile) {
        super(profile);
    }

    protected boolean hurtBy(SkyBlockPlayer player, Damage damage) {
        return damage.getType() == net.minestom.server.entity.damage.DamageType.PLAYER_ATTACK
                && player.getPosition().distance(getPosition()) <= MELEE_RANGE;
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return hurtBy(player, damage);
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (++underAttack < TELEPORT_AFTER / 10) return;
        underAttack = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        teleport(getPosition().add(random.nextDouble(-6, 6), 0, random.nextDouble(-6, 6)));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 40 != 0) return;
        rangedTarget(20).ifPresent(player -> {
            for (int i = 0; i < 6; i++) fireball(player, getProfile().damage());
        });
    }
}
