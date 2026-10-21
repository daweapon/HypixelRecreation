package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService;

final class TimedFlyAway5m implements CreatureSpecial {
    static final long STAY_TICKS = 5 * 60 * 20;
    private static final long CLIMB_TICKS = 60;
    private static final double CLIMB_PER_TICK = 0.4;
    private static final double ANNOUNCE_RANGE = 64;

    private final PerMob<long[]> departure = new PerMob<>();

    static boolean expired(long aliveTicks) {
        return aliveTicks >= STAY_TICKS;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        long[] left = departure.of(mob, () -> new long[]{-1});
        if (left[0] < 0) {
            if (!expired(ticks) || LassoService.isLassoed(mob) || mob.isCaptured()) return;
            left[0] = ticks;
            announce(mob);
        }
        if (ticks - left[0] >= CLIMB_TICKS) {
            departure.clear(mob);
            mob.remove();
            return;
        }
        CreatureMotion.fly(mob, mob.getPosition().add(0, 1, 0), CLIMB_PER_TICK);
    }

    private void announce(HuntingCreatureMob mob) {
        CreatureMotion.nearestPlayer(mob, ANNOUNCE_RANGE).ifPresent(player -> CreatureMotion.say(mob, player, "flew_away"));
    }
}
