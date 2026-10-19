package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEndermiteHigh extends EndMob {
    public MobEndermiteHigh() {
        super(EntityType.ENDERMITE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.ENDERMITE_HIGH;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

}
