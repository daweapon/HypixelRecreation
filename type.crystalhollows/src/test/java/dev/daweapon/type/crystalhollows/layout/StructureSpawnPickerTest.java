package dev.daweapon.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.layout.StructureSpawnPicker.Box;
import dev.daweapon.type.crystalhollows.layout.StructureSpawnPicker.Terrain;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureSpawnPickerTest {
    private static final Terrain FLOOR_AT_ZERO = new Terrain() {
        @Override
        public boolean isSolid(int x, int y, int z) {
            return y <= 0;
        }

        @Override
        public boolean isAir(int x, int y, int z) {
            return y > 0;
        }
    };

    private static final Terrain SOLID = new Terrain() {
        @Override
        public boolean isSolid(int x, int y, int z) {
            return true;
        }

        @Override
        public boolean isAir(int x, int y, int z) {
            return false;
        }
    };

    @Test
    void picksAirAboveSolidFloorInsideBox() {
        Box box = new Box(10, 0, 20, 30, 10, 40);
        for (int seed = 0; seed < 50; seed++) {
            Pos pos = StructureSpawnPicker.pickIn(box, FLOOR_AT_ZERO, new Random(seed)).orElseThrow();
            assertTrue(pos.blockX() >= 10 && pos.blockX() <= 30);
            assertTrue(pos.blockZ() >= 20 && pos.blockZ() <= 40);
            assertEquals(1.0, pos.y());
        }
    }

    @Test
    void requiresTwoBlocksOfHeadroom() {
        Terrain lowCeiling = new Terrain() {
            @Override
            public boolean isSolid(int x, int y, int z) {
                return y <= 0 || y >= 2;
            }

            @Override
            public boolean isAir(int x, int y, int z) {
                return !isSolid(x, y, z);
            }
        };
        assertTrue(StructureSpawnPicker.pickIn(new Box(0, 0, 0, 5, 6, 5), lowCeiling, new Random(1)).isEmpty());
    }

    @Test
    void fullySolidBoxHasNoSpawn() {
        assertTrue(StructureSpawnPicker.pickIn(new Box(0, 0, 0, 5, 5, 5), SOLID, new Random(2)).isEmpty());
    }

    @Test
    void fallsThroughToAnotherBoxWhenFirstIsSolid() {
        Terrain mixed = new Terrain() {
            @Override
            public boolean isSolid(int x, int y, int z) {
                return x < 100 || y <= 0;
            }

            @Override
            public boolean isAir(int x, int y, int z) {
                return !isSolid(x, y, z);
            }
        };
        List<Box> boxes = List.of(new Box(0, 0, 0, 10, 10, 10), new Box(100, 0, 0, 110, 10, 10));
        for (int seed = 0; seed < 20; seed++) {
            Optional<Pos> pos = StructureSpawnPicker.pick(boxes, mixed, new Random(seed));
            assertTrue(pos.isPresent());
            assertTrue(pos.get().blockX() >= 100);
        }
    }

    @Test
    void mobFreeStructuresAreNeverEligible() {
        StructureSite site = new StructureSite(0, 0, 0, 9, 9, 9);
        StructureSpec divan = spec("mines_of_divan", RegionType.MINES_OF_DIVAN, site);
        StructureSpec khazad = spec("khazad_dum", RegionType.KHAZAD_DUM, site);
        StructureSpec noSite = spec("no_site", RegionType.JUNGLE_TEMPLE, null);
        List<Box> boxes = StructureSpawnPicker.eligibleBoxes(List.of(
                new StructurePlacement(divan, new Pos(100, 50, 100)),
                new StructurePlacement(khazad, new Pos(200, 60, 200)),
                new StructurePlacement(noSite, new Pos(300, 60, 300))));
        assertEquals(List.of(new Box(200, 60, 200, 209, 69, 209)), boxes);
    }

    private static StructureSpec spec(String id, RegionType region, StructureSite site) {
        return new StructureSpec(id, id, region, false, 0, List.of(), Map.of(), null, site, null, List.of());
    }
}
