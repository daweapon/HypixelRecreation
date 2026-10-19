package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobVoidlingExtremist extends EndMob {
    public MobVoidlingExtremist() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.VOIDLING_EXTREMIST;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.VOID_SEPULTURE;
    }

}
