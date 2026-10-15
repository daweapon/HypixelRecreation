package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

public class MobThyst extends CrystalHollowsMob {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("THYST", "Thyst", 39, 5_000, 250)
            .bestiary(5, 15, Material.AMETHYST_SHARD)
            .rewards(58, 13, 42)
            .types(MobType.ARTHROPOD)
            .aggroRange(10)
            .drops(Drop.always(ItemType.ROUGH_AMETHYST_GEM, 4, 7));

    public MobThyst() {
        super(EntityType.ENDERMITE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.JUNGLE;
    }
}
