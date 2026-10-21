package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

/** One line per special id; each implementation lives in its own class in this package. */
final class SpecialRegistrations {
    private SpecialRegistrations() {
    }

    static void registerAll() {
        NetSpecials.register();
        LassoSpecials.register();
        InteractSpecials.register();
    }
}
