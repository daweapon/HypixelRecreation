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

public class MobAutomaton67 extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("AUTOMATON_MONSTER", "Automaton", 67, 15_000, 750)
            .bestiary(1, 10, Material.IRON_BLOCK)
            .rewards(100, 22, 73)
            .types(MobType.CONSTRUCT)
            .drops(
                    Drop.always(ItemType.ROUGH_SAPPHIRE_GEM, 8, 15))
            .exclusiveDrops(
                    Drop.single(ItemType.ELECTRON_TRANSMITTER, 1),
                    Drop.single(ItemType.FTX_3070, 1),
                    Drop.single(ItemType.ROBOTRON_REFLECTOR, 1),
                    Drop.single(ItemType.SUPERLITE_MOTOR, 1),
                    Drop.single(ItemType.CONTROL_SWITCH, 1),
                    Drop.single(ItemType.SYNTHETIC_HEART, 1));

    public MobAutomaton67() {
        super(EntityType.IRON_GOLEM);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.PRECURSOR_REMNANTS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.PRECURSOR_REMNANTS, 10));
    }
}
