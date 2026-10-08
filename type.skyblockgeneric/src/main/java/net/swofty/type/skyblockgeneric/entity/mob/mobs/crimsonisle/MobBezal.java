package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonBlazeMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Bezal (wiki: Bezal): a Blaze that burns its killer for 1,000 damage when it dies.
 */
public class MobBezal extends CrimsonBlazeMob {
    public MobBezal() {
        super(CrimsonMobs.BEZAL);
    }

    private static final double DEATH_DAMAGE = 1_000;

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        trueStrike(killer, DEATH_DAMAGE);
        killer.setFireTicks(60);
        killer.sendMessage("<7>The nearby blaze died and set you on fire for <c>1000 <7>damage!");
    }
}
