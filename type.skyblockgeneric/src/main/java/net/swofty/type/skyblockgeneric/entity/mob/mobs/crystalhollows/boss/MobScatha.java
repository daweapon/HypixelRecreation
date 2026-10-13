package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class MobScatha extends MobWorm {

    public MobScatha() {
        super(CrystalHollowsBossProfiles.SCATHA);
    }

    @Override
    protected void reward(SkyBlockPlayer player) {
        PetDropTable.of("SCATHA_PET")
                .rarity(Rarity.RARE, 0.24)
                .rarity(Rarity.EPIC, 0.12)
                .rarity(Rarity.LEGENDARY, 0.04)
                .roll(player, this);
    }
}
