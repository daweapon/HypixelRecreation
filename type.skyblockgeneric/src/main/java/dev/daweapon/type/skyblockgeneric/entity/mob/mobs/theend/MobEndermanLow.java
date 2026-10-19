package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.PetDropTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEndermanLow extends MobEnderman implements RegionPopulator {
    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.ENDERMAN_LOW;
    }

    @Override
    protected PetDropTable petDrops() {
        return PetDropTable.of("ENDERMAN_PET")
                .rarity(Rarity.UNCOMMON, 0.02)
                .rarity(Rarity.COMMON, 0.05)
                .rarity(Rarity.RARE, 0.01);
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.THE_END, 3));
    }
}
