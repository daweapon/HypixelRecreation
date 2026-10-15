package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;

public class MobExecutiveWendy extends MobTreasuriteExecutive {
    private static final CrystalHollowsMobSpec SPEC = executive("TEAM_TREASURITE_WENDY", "Executive Wendy");

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }
}
