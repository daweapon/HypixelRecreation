package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalHollowsLayoutTest {
    private static StructureSpec spec(String id, int clearance, boolean optional, Pos... candidates) {
        return new StructureSpec(id, id, RegionType.JUNGLE, optional, clearance, List.of(candidates),
                Map.of("npc", new Pos(1, 2, 3, 90, 0)));
    }

    @Test
    void shippedCatalogueParsesAndPlacesTheCapturedStructures() {
        List<StructureSpec> specs = StructureCatalogue.load(java.nio.file.Path.of("../configuration/skyblock/crystal_hollows/structures.yml"));
        assertFalse(specs.isEmpty());
        CrystalHollowsLayout.generate(specs, 1L);
        assertTrue(CrystalHollowsLayout.isPlaced("jungle_temple"));
        assertTrue(CrystalHollowsLayout.isPlaced("khazad_dum"));
        assertNotNull(CrystalHollowsLayout.anchor("khazad_dum", "bal"));
        assertTrue(CrystalHollowsLayout.isPlaced("goblin_queens_den"));
        assertTrue(CrystalHollowsLayout.isPlaced("king_yolkars_tower"));
        assertTrue(CrystalHollowsLayout.isPlaced("odawa_hut"));
        assertTrue(CrystalHollowsLayout.isPlaced("lost_precursor_city"));
        assertTrue(CrystalHollowsLayout.isPlaced("mines_of_divan"));
    }

    @Test
    void placementsAreDeterministicForASeedAndVaryAcrossSeeds() {
        StructureSpec temple = spec("temple", 10, false, new Pos(0, 0, 0), new Pos(100, 0, 0), new Pos(200, 0, 0), new Pos(300, 0, 0));
        CrystalHollowsLayout.generate(List.of(temple), 42L);
        Pos first = CrystalHollowsLayout.origin("temple");
        CrystalHollowsLayout.generate(List.of(temple), 42L);
        assertEquals(first, CrystalHollowsLayout.origin("temple"));
        Set<Pos> seen = new java.util.HashSet<>();
        for (long seed = 0; seed < 40; seed++) {
            CrystalHollowsLayout.generate(List.of(temple), seed);
            seen.add(CrystalHollowsLayout.origin("temple"));
        }
        assertTrue(seen.size() > 1);
    }

    @Test
    void structuresKeepTheirClearanceApart() {
        StructureSpec a = spec("a", 50, false, new Pos(0, 0, 0), new Pos(200, 0, 0));
        StructureSpec b = spec("b", 50, false, new Pos(10, 0, 0), new Pos(210, 0, 0));
        for (long seed = 0; seed < 30; seed++) {
            CrystalHollowsLayout.generate(List.of(a, b), seed);
            Pos first = CrystalHollowsLayout.origin("a");
            Pos second = CrystalHollowsLayout.origin("b");
            assertNotNull(first);
            assertNotNull(second);
            assertTrue(Math.abs(first.x() - second.x()) >= 50);
        }
    }

    @Test
    void anchorsAreOffsetFromTheOrigin() {
        CrystalHollowsLayout.generate(List.of(spec("hut", 10, false, new Pos(100, 50, 200))), 7L);
        Pos anchor = CrystalHollowsLayout.anchor("hut", "npc");
        assertEquals(new Pos(101, 52, 203, 90, 0), anchor);
    }

    @Test
    void anUnplaceableOptionalStructureIsSkipped() {
        StructureSpec fixed = spec("fixed", 100, false, new Pos(0, 0, 0));
        StructureSpec crowded = spec("crowded", 100, true, new Pos(5, 0, 5));
        CrystalHollowsLayout.generate(List.of(fixed, crowded), 3L);
        assertEquals(1, CrystalHollowsLayout.placements().size());
    }
}
