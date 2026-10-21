package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;

final class CurlWhenNear10 implements CreatureSpecial {
    private static final double CURL_RANGE = 10;
    private static final long UNCURL_DELAY_TICKS = 40;
    private static final long CHECK_PERIOD = 5;

    private final PerMob<long[]> lastSeen = new PerMob<>();

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        long[] seen = lastSeen.of(mob, () -> new long[]{Long.MIN_VALUE / 2});
        if (ticks % CHECK_PERIOD == 0 && playerInSight(mob)) seen[0] = ticks;
        boolean curled = ticks - seen[0] < UNCURL_DELAY_TICKS;
        if (mob.getEntityMeta() instanceof ArmadilloMeta meta) {
            ArmadilloMeta.State wanted = curled ? ArmadilloMeta.State.ROLLING : ArmadilloMeta.State.IDLE;
            if (meta.getState() != wanted) meta.setState(wanted);
        }
        if (!curled) return;
        mob.getVanillaNavigator().stop();
        mob.setVelocity(new Vec(0, mob.getVelocity().y(), 0));
    }

    private boolean playerInSight(HuntingCreatureMob mob) {
        return CreatureMotion.nearestPlayer(mob, CURL_RANGE)
                .filter(player -> CreatureMotion.lineOfSight(mob.getInstance(),
                        mob.getPosition().add(0, 0.5, 0), player.getPosition().add(0, player.getEyeHeight(), 0)))
                .isPresent();
    }
}
