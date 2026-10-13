package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobGrunt extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("TEAM_TREASURITE_GRUNT", "Grunt", 54, 15_000, 300)
            .bestiary(1, 10, Material.SILVERFISH_SPAWN_EGG)
            .rewards(81, 18, 59)
            .drops(
                    Drop.always(ItemType.ROUGH_RUBY_GEM, 8, 15),
                    Drop.always(ItemType.ROUGH_JADE_GEM, 8, 15),
                    Drop.single(ItemType.TREASURITE, 1));

    public MobGrunt() {
        super(EntityType.ZOMBIE);
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
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.MITHRIL_DEPOSITS, 8));
    }
}
