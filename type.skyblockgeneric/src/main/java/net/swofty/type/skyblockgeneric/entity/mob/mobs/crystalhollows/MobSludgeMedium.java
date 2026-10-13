package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.function.Supplier;

public class MobSludgeMedium extends MobSludge {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("SLUDGE_10", "Sludge", 60, 10_000, 700)
            .bestiary(1, 10, Material.SLIME_BALL)
            .rewards(90, 20, 66)
            .types(MobType.CUBIC);

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected int slimeSize() {
        return 2;
    }

    @Override
    protected Supplier<? extends MobSludge> splitInto() {
        return MobSludgeSmall::new;
    }

    @Override
    protected int populationTarget() {
        return 4;
    }
}
