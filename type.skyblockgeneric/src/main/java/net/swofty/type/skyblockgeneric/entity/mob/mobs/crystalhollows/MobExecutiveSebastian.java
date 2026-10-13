package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;

public class MobExecutiveSebastian extends MobTreasuriteExecutive {
    private static final CrystalHollowsMobSpec SPEC = executive("TEAM_TREASURITE_SEBASTIAN", "Executive Sebastian");

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }
}
