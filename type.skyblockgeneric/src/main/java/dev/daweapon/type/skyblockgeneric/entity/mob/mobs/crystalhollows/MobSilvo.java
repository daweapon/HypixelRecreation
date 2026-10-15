package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

public class MobSilvo extends CrystalHollowsMob {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("SILVO", "Silvo", 54, 6_500, 300)
            .bestiary(1, 10, Material.SILVERFISH_SPAWN_EGG)
            .types(MobType.ARTHROPOD)
            .aggroRange(16);

    public MobSilvo() {
        super(EntityType.SILVERFISH);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.MITHRIL_DEPOSITS;
    }

    @Override
    public boolean countsForBestiary() {
        return false;
    }
}
