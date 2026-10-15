package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

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
