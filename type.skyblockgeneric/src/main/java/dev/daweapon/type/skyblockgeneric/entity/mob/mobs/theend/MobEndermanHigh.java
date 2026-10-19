package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.PetDropTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEndermanHigh extends MobEnderman implements RegionPopulator {
    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.ENDERMAN_HIGH;
    }

    @Override
    protected PetDropTable petDrops() {
        return PetDropTable.of("ENDERMAN_PET")
                .rarity(Rarity.RARE, 0.1)
                .rarity(Rarity.EPIC, 0.01)
                .rarity(Rarity.LEGENDARY, 0.0006);
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.THE_END, 1));
    }
}
