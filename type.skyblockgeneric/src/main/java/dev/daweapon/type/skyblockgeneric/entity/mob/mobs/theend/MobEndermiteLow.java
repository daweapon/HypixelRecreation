package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEndermiteLow extends EndMob implements RegionPopulator {
    public MobEndermiteLow() {
        super(EntityType.ENDERMITE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.ENDERMITE_LOW;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.THE_END;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.THE_END, 2));
    }
}
