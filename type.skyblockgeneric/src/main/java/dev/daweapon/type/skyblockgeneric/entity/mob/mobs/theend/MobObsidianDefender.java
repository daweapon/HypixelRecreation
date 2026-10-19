package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobObsidianDefender extends EndMob implements RegionPopulator {
    public MobObsidianDefender() {
        super(EntityType.WITHER_SKELETON);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setHelmet(ItemStack.of(Material.OBSIDIAN));
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.OBSIDIAN_DEFENDER;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    protected List<EndMobSpecs.Extra> extras() {
        return EndMobSpecs.OBSIDIAN_DEFENDER_EXTRAS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.DRAGONS_NEST, 4));
    }
}
