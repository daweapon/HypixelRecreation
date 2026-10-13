package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.function.Supplier;

public class MobSludgeSmall extends MobSludge {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("SLUDGE_5", "Sludge", 47, 5_000, 500)
            .bestiary(1, 10, Material.SLIME_BALL)
            .rewards(70, 15, 51)
            .types(MobType.CUBIC)
            .drops(Drop.always(ItemType.SLUDGE_JUICE, 1, 1));

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected int slimeSize() {
        return 1;
    }

    @Override
    protected Supplier<? extends MobSludge> splitInto() {
        return null;
    }

    @Override
    protected int populationTarget() {
        return 4;
    }
}
