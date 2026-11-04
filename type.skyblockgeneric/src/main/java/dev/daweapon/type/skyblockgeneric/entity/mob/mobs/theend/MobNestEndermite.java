package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

public class MobNestEndermite extends EndMob {
    private static final ThreadLocal<RegionType> CONSTRUCTING = new ThreadLocal<>();

    private final RegionType home;

    public MobNestEndermite() {
        this(RegionType.DRAGONS_NEST);
    }

    public MobNestEndermite(RegionType home) {
        super(begin(home));
        this.home = home;
        CONSTRUCTING.remove();
    }

    private static EntityType begin(RegionType home) {
        CONSTRUCTING.set(home);
        return EntityType.ENDERMITE;
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.NEST_ENDERMITE;
    }

    @Override
    protected RegionType homeRegion() {
        return home != null ? home : CONSTRUCTING.get();
    }
}
