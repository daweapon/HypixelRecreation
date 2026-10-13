package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobYog extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("YOG", "Yog", 93, 35_000, 1_500)
            .bestiary(1, 10, Material.MAGMA_CREAM)
            .rewards(139, 31, 102)
            .types(MobType.CUBIC, MobType.MAGMATIC)
            .drops(
                    Drop.always(ItemType.ROUGH_TOPAZ_GEM, 8, 15),
                    Drop.single(ItemType.YOGGIE, 6));

    public MobYog() {
        super(EntityType.MAGMA_CUBE);
    }

    @Override
    public void onInit() {
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(4));
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.MAGMA_FIELDS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.MAGMA_FIELDS, 5));
    }
}
