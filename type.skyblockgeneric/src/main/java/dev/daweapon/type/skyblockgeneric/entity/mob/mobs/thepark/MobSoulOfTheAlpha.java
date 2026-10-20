package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark;

import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;

public class MobSoulOfTheAlpha extends HowlingCaveWolf {
    @Override
    protected CrystalHollowsMobSpec spec() {
        return HowlingCaveMobSpecs.SOUL_OF_THE_ALPHA;
    }

    @Override
    protected String texture() {
        return HowlingCaveMobSpecs.ALPHA_TEXTURE;
    }
}
