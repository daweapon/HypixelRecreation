package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class InteractSpecials {
    private static final int HOPS_TO_EXHAUST = 4;
    private static final int RETURNS_TO_CAPTURE = 3;
    private static final double BOW_RANGE = 10;
    private static final long BOW_INTERVAL_TICKS = 40;
    private static final double AXE_RANGE = 14;
    private static final long AXE_INTERVAL_TICKS = 60;
    private static final double HEAL_TRIGGER = 0.3;
    private static final double HEAL_FRACTION = 0.25;

    private InteractSpecials() {
    }

    static void register() {
        CreatureSpecials.register("CLICK_HOP_4", new ClickHopSpecial(HOPS_TO_EXHAUST));
        CreatureSpecials.register("FEED_BAMBOO_40_44", new FeedBambooSpecial());
        CreatureSpecials.register("REFLECT_BULLET_3", new ReflectBulletSpecial(RETURNS_TO_CAPTURE));
        CreatureSpecials.register("RANGED_BOW_10", ProjectileSpecial.bow(BOW_RANGE, BOW_INTERVAL_TICKS));
        CreatureSpecials.register("THROW_AXE_500", ProjectileSpecial.axe(AXE_RANGE, AXE_INTERVAL_TICKS));
        CreatureSpecials.register("TOTEM_PUZZLE", new TotemPuzzleSpecial());
        CreatureSpecials.register("VACUUM_ONLY", new VacuumOnlySpecial());
        CreatureSpecials.register("SELF_HEAL_ONCE_25", new SelfHealSpecial(HEAL_TRIGGER, HEAL_FRACTION));
        CreatureSpecials.register("QUEEN_GUARDED_BY_ANTS", new QueenGuardSpecial());
    }
}
