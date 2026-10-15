package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public abstract class MobTreasuriteExecutive extends CrystalHollowsMob implements RegionPopulator {
    protected static CrystalHollowsMobSpec executive(String id, String name) {
        return CrystalHollowsMobSpec.of(id, name, 62, 18_000, 450)
                .bestiary(1, 10, Material.SILVERFISH_SPAWN_EGG)
                .rewards(93, 20, 68)
                .drops(
                        Drop.always(ItemType.ROUGH_RUBY_GEM, 8, 15),
                        Drop.always(ItemType.ROUGH_JADE_GEM, 8, 15),
                        Drop.single(ItemType.TREASURITE, 1));
    }

    protected MobTreasuriteExecutive() {
        super(HollowsSkins.PLAYER_MODEL);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        HollowsSkins.dress(this, HollowsSkins.GRUNT);
        setItemInMainHand(ItemStack.of(Material.IRON_PICKAXE));
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.MITHRIL_DEPOSITS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.MITHRIL_DEPOSITS, 3));
    }
}
