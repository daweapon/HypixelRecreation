package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.PetDropTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class MobVoidlingFanatic extends EndMob {
    public MobVoidlingFanatic() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.VOIDLING_FANATIC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.VOID_SEPULTURE;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer player, Instance instance, Pos position) {
        PetDropTable.of("ENDERMAN_PET")
                .rarity(Rarity.EPIC, 0.012)
                .rarity(Rarity.LEGENDARY, 0.0007)
                .roll(player, this);
    }

}
