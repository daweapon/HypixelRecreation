package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShaftTypeTest {
    @Test
    void thereAreSeventeenTypesAndTwentyNineLayouts() {
        assertEquals(17, ShaftType.values().length);
        Set<String> codes = new HashSet<>();
        for (ShaftType type : ShaftType.values()) type.layouts().forEach(layout -> codes.add(layout.code()));
        assertEquals(29, codes.size());
    }

    @Test
    void layoutCodesFollowTheScoreboardFormat() {
        assertEquals("RUBY_2", ShaftType.parseLayout("ruby_2").code());
        assertEquals("RUBY_C", ShaftType.RUBY.crystalLayout().code());
        assertEquals("LITT_L", ShaftType.LITT.plainLayouts().getFirst().code());
        assertEquals("FAIR_1", ShaftType.FAIR.plainLayouts().getFirst().code());
        assertNull(ShaftType.parseLayout("TITA_C"));
        assertNull(ShaftType.TITA.crystalLayout());
    }

    @Test
    void crystalLayoutsExistOnlyForTheCrystalGems() {
        Set<ShaftType> crystal = Set.of(ShaftType.JASP, ShaftType.RUBY, ShaftType.OPAL, ShaftType.ONYX, ShaftType.PERI,
                ShaftType.CITR, ShaftType.AQUA);
        for (ShaftType type : ShaftType.values()) assertEquals(crystal.contains(type), type.hasCrystalLayout(), type.name());
    }

    @Test
    void minedBlocksMatchTheirShaftType() {
        assertEquals(ShaftType.UMBE, ShaftType.matching(MineableBlock.UMBER_ORE));
        assertEquals(ShaftType.TUNG, ShaftType.matching(MineableBlock.TUNGSTEN_ORE));
        assertEquals(ShaftType.AQUA, ShaftType.matching(MineableBlock.AQUAMARINE_ORE));
        assertEquals(ShaftType.TITA, ShaftType.matching(MineableBlock.TITANIUM));
        assertNull(ShaftType.matching(MineableBlock.GLACITE_ORE));
        assertNull(ShaftType.matching(null));
    }
}
