package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobKalhuikiTribeMember extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("KALHUIKI_TRIBE_MEMBER", "Kalhuiki Tribe Member", 772, 1_000_000_000, 800_000)
            .bestiary(1, 10, Material.PLAYER_HEAD)
            .rewards(1_158, 257, 849)
            .types(MobType.HUMANOID, MobType.WOODLAND)
            .provokedOnly();

    public MobKalhuikiTribeMember() {
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
        return RegionType.JUNGLE;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, 3));
    }
}
