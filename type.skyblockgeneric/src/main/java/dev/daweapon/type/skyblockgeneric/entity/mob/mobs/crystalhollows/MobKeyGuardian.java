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

public class MobKeyGuardian extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("KEY_GUARDIAN", "Key Guardian", 120, 250_000, 100)
            .bestiary(7, 15, Material.TRIPWIRE_HOOK)
            .rewards(900, 300, 360)
            .types(MobType.HUMANOID, MobType.WOODLAND)
            .drops(Drop.always(ItemType.JUNGLE_KEY, 1, 1));

    public MobKeyGuardian() {
        super(EntityType.ZOMBIE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.JUNGLE;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, 2));
    }
}
