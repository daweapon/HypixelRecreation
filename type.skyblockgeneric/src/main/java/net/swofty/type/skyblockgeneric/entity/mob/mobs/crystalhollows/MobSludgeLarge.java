package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.function.Supplier;

public class MobSludgeLarge extends MobSludge {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("SLUDGE_100", "Sludge", 78, 25_000, 900)
            .bestiary(1, 10, Material.SLIME_BALL)
            .rewards(117, 26, 85)
            .types(MobType.CUBIC);

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected int slimeSize() {
        return 4;
    }

    @Override
    protected Supplier<? extends MobSludge> splitInto() {
        return MobSludgeMedium::new;
    }

    @Override
    protected int populationTarget() {
        return 5;
    }
}
