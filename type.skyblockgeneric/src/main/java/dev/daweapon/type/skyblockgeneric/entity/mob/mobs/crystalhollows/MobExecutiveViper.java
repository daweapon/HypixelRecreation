package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;

public class MobExecutiveViper extends MobTreasuriteExecutive {
    private static final CrystalHollowsMobSpec SPEC = executive("TEAM_TREASURITE_VIPER", "Executive Viper");

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }
}
