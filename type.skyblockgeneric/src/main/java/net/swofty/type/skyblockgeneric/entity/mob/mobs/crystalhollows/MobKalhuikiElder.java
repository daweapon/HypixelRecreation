package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobKalhuikiElder extends KalhuikiMob {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("KALHUIKI_ELDER", "Kalhuiki Elder", 500, 500_000_000, 500_000)
            .bestiary(1, 10, Material.PLAYER_HEAD)
            .rewards(800, 180, 600)
            .types(MobType.HUMANOID, MobType.WOODLAND)
            .provokedOnly();

    public MobKalhuikiElder() {
        super();
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, 2));
    }
}
