package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

/** The wither Boss Corleone calls down. It hovers where it lands and fires at the nearest player; the fire rate and range are estimates. */
public class MobSmog extends CrimsonIsleMob {
    private static final double RANGE = 20;
    private static final int FIRE_INTERVAL = 60;

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
    protected void onAiTick(int ticks) {
        if (ticks % FIRE_INTERVAL != 0) return;
        Optional<SkyBlockPlayer> target = rangedTarget(RANGE);
        target.ifPresent(player -> fireball(player, getProfile().damage()));
    }
}
