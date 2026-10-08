package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Hellwisp (wiki: Hellwisp): hovers over the lava pools outside the Matriarch's Lair and lashes a tentacle that
 * reels the player into its mouth. Tentacle timing and bite damage are estimates.
 */
public class MobHellwisp extends CrimsonIsleMob {
    public MobHellwisp() {
        super(CrimsonMobs.HELLWISP);
    }

    private static final int TENTACLE_INTERVAL_TICKS = 100;
    private static final int REEL_TICKS = 30;
    private static final double BITE_DAMAGE_PER_TICK = 250;
    private SkyBlockPlayer caught;
    private int reel;

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        getAttribute(Attribute.SCALE).setBaseValue(0.5);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (caught != null) {
            reelIn();
            return;
        }
        if (ticks % TENTACLE_INTERVAL_TICKS != 0) return;
        rangedTarget(14).ifPresent(player -> {
            caught = player;
            reel = REEL_TICKS;
        });
    }

    private void reelIn() {
        if (caught.isDead() || caught.getInstance() != getInstance()) {
            caught = null;
            return;
        }
        LavaBeam.draw(getInstance(), getPosition().add(0, 1, 0), caught.getPosition().add(0, 1, 0));
        Vec pull = getPosition().sub(caught.getPosition()).asVec();
        if (pull.length() > 2) {
            caught.setVelocity(pull.normalize().mul(10));
        } else {
            trueStrike(caught, BITE_DAMAGE_PER_TICK * 5);
        }
        if (--reel <= 0) caught = null;
    }
}
