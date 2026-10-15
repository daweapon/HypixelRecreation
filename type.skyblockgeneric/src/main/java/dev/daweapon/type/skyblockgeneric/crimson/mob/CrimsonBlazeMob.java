package dev.daweapon.type.skyblockgeneric.crimson.mob;

import net.minestom.server.coordinate.Vec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

/**
 * Blaze AI (wiki: Blaze): idles on the ground, and on sensing a player rises to 0.5-3.5 blocks above them,
 * fires a volley of 5 fireballs 0.2 s apart within about 16 blocks, then rests about 2 s.
 */
public abstract class CrimsonBlazeMob extends CrimsonIsleMob {
    private static final int VOLLEY_SIZE = 5;
    private static final int SHOT_SPACING_TICKS = 4;
    private static final int REST_TICKS = 40;
    private static final double HOVER_ABOVE = 2;

    private int shotsLeft;
    private int cooldown;

    protected CrimsonBlazeMob(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected boolean melee() {
        return false;
    }

    protected double attackRange() {
        return 16;
    }

    protected boolean flies() {
        return true;
    }

    @Override
    protected void onAiTick(int ticks) {
        Optional<SkyBlockPlayer> target = rangedTarget(attackRange());
        if (target.isEmpty()) {
            if (flies() && hasNoGravity()) setNoGravity(false);
            shotsLeft = 0;
            return;
        }
        SkyBlockPlayer player = target.get();
        lookAt(player);
        if (flies()) hover(player);
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (shotsLeft == 0) shotsLeft = VOLLEY_SIZE;
        fireball(player, getProfile().damage());
        shotsLeft--;
        cooldown = shotsLeft == 0 ? REST_TICKS : SHOT_SPACING_TICKS;
    }

    private void hover(SkyBlockPlayer player) {
        setNoGravity(true);
        double wanted = player.getPosition().y() + HOVER_ABOVE - getPosition().y();
        setVelocity(new Vec(getVelocity().x(), Math.max(-4, Math.min(4, wanted * 2)), getVelocity().z()));
    }
}
