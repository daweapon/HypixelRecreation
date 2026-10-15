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

public class MobAutomaton71 extends CrystalHollowsMob implements RegionPopulator {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("AUTOMATON_MONSTER_71", "Automaton", 71, 20_000, 750)
            .bestiary(1, 10, Material.IRON_BLOCK)
            .rewards(106, 23, 78)
            .types(MobType.CONSTRUCT)
            .drops(
                    Drop.always(ItemType.ROUGH_SAPPHIRE_GEM, 10, 18))
            .exclusiveDrops(
                    Drop.single(ItemType.ELECTRON_TRANSMITTER, 2),
                    Drop.single(ItemType.FTX_3070, 2),
                    Drop.single(ItemType.ROBOTRON_REFLECTOR, 2),
                    Drop.single(ItemType.SUPERLITE_MOTOR, 2),
                    Drop.single(ItemType.CONTROL_SWITCH, 2),
                    Drop.single(ItemType.SYNTHETIC_HEART, 2));

    public MobAutomaton71() {
        super(EntityType.IRON_GOLEM);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.LOST_PRECURSOR_CITY;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.LOST_PRECURSOR_CITY, 8));
    }
}
