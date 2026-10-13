package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobCryptUndead extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("CRYPT_UNDEAD_MITHRIL", "Crypt Undead", 50, 12_000, 300)
            .bestiary(1, 10, Material.ZOMBIE_HEAD)
            .rewards(70, 15, 50)
            .types(MobType.UNDEAD)
            .drops(
                    Drop.single(ItemType.PREMIUM_FLESH, 2),
                    Drop.single(ItemType.REVIVE_STONE, 0.5));

    public MobCryptUndead() {
        super(EntityType.ZOMBIE);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    public boolean countsForBestiary() {
        return false;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.MITHRIL_DEPOSITS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.MITHRIL_DEPOSITS, 6));
    }
}
