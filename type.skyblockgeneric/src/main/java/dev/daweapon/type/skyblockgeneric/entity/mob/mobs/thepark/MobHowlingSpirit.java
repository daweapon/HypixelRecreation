package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark;

import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;

public class MobHowlingSpirit extends HowlingCaveWolf {
    @Override
    protected CrystalHollowsMobSpec spec() {
        return HowlingCaveMobSpecs.HOWLING_SPIRIT;
    }

    @Override
    protected String texture() {
        return HowlingCaveMobSpecs.SPIRIT_TEXTURE;
    }
}
