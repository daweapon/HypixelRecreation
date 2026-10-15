package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobButterfly extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("BUTTERFLY", "Butterfly", 6, 100, 100)
            .bestiary(7, 15, Material.PINK_DYE)
            .rewards(9, 2, 6)
            .types(MobType.ANIMAL, MobType.AIRBORNE)
            .drops(
                    Drop.always(ItemType.ROUGH_JASPER_GEM, 10, 22),
                    Drop.single(ItemType.JASPER_CRYSTAL, 0.1));

    public MobButterfly() {
        super(EntityType.BEE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.FAIRY_GROTTO;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.FAIRY_GROTTO, 4));
    }
}
