package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.PetDropTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

abstract class HowlingCaveWolf extends CrystalHollowsMob {
    protected HowlingCaveWolf() {
        super(EntityType.WOLF);
    }

    protected abstract String texture();

    @Override
    protected RegionType homeRegion() {
        return RegionType.HOWLING_CAVE;
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(texture());
    }

    @Override
    public void kill() {
        Optional<SkyBlockPlayer> killer = getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
                ? Optional.of(player) : Optional.empty();
        super.kill();
        killer.ifPresent(player -> houndDrops().roll(player, this));
    }

    private static PetDropTable houndDrops() {
        PetDropTable table = PetDropTable.of(HowlingCaveMobSpecs.HOUND_PET);
        for (HowlingCaveMobSpecs.HoundChance chance : HowlingCaveMobSpecs.HOUND_CHANCES) {
            table.rarity(chance.rarity(), chance.chancePercent());
        }
        return table;
    }
}
