package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class NetSpecials {
    private static final double STAND_STILL_RADIUS = 12;
    private static final long TICKS_PER_SECOND = 20;

    private NetSpecials() {
    }

    static void register() {
        CreatureSpecials.register("SPIN_METER_90", new SpinMeterSpecial());
        CreatureSpecials.register("PULL_SYMBOLS_4", new PullSymbolsSpecial());
        CreatureSpecials.register("STAND_STILL_8S", new StandStillSpecial(STAND_STILL_RADIUS, 0, 5 * TICKS_PER_SECOND, 8 * TICKS_PER_SECOND));
        CreatureSpecials.register("STAND_STILL_10S", new StandStillSpecial(STAND_STILL_RADIUS, TICKS_PER_SECOND, 6 * TICKS_PER_SECOND, 10 * TICKS_PER_SECOND));
        CreatureSpecials.register("CAVE_HIDE_3", new CaveHideSpecial());
        CreatureSpecials.register("SHRINK_TELEPORT_3", new ShrinkTeleportSpecial());
        CreatureSpecials.register("SHARDS_BY_MAX_HP", new ShardsByMaxHpSpecial());
        CreatureSpecials.register("ONLY_FROM_10_BLOCKS", new OnlyFromDistanceSpecial(10));
        CreatureSpecials.register("NET_ANGERS_ATTACK", new AngerOnNetSpecial());
    }
}
