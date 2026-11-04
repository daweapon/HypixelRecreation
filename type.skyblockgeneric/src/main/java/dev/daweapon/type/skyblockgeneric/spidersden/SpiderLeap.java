package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The pounce the wiki gives Dasher Spiders ("launches itself towards the player") and Arachne's Brood ("run and
 * jump towards players"). The wiki gives no distances, so range, cadence and launch speed are estimates.
 */
public record SpiderLeap(int intervalTicks, double minRange, double maxRange, double speed, double lift) {
    public static final SpiderLeap DASHER = new SpiderLeap(60, 3, 10, 14, 7);
    public static final SpiderLeap BROOD = new SpiderLeap(40, 3, 8, 12, 6);

    public void tick(SpiderDenMob mob, int ticks) {
        if (ticks % intervalTicks != 0 || !mob.isOnGround()) return;
        mob.leapTarget(maxRange).ifPresent(target -> launch(mob, target));
    }

    private void launch(SpiderDenMob mob, SkyBlockPlayer target) {
        Pos from = mob.getPosition();
        Pos to = target.getPosition();
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < minRange) return;
        mob.setVelocity(new Vec(dx / distance * speed, lift, dz / distance * speed));
    }
}
