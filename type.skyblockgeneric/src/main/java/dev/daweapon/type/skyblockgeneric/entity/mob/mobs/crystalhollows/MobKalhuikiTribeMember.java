package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobKalhuikiTribeMember extends KalhuikiMob {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("KALHUIKI_TRIBE_MEMBER", "Kalhuiki Tribe Member", 772, 1_000_000_000, 800_000)
            .bestiary(1, 10, Material.PLAYER_HEAD)
            .rewards(1_158, 257, 849)
            .types(MobType.HUMANOID, MobType.WOODLAND)
            .provokedOnly();

    public MobKalhuikiTribeMember() {
        super();
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, 5));
    }
}
