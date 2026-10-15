package net.swofty.type.crystalhollows.divan;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DivanDigSiteTest {
    private static List<Pos> offsets(int count) {
        List<Pos> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(new Pos(i, 0, i));
        return list;
    }

    @Test
    void shippedConfigIsEmptyUntilTheWorldExists() {
        DivanDigSite.Config config = DivanDigSite.read(Path.of("../configuration/skyblock/crystal_hollows/metal_detector.yml"));
        assertEquals("mines_of_divan", config.structure());
        assertTrue(config.offsets().isEmpty());
        assertEquals(DivanDigSite.DEFAULT_LOBBY_SPOTS, config.lobbySpots());
    }

    @Test
    void selectionIsDeterministicPerSeedAndCappedByCandidates() {
        List<Pos> all = offsets(40);
        assertEquals(DivanDigSite.select(all, 16, 5L), DivanDigSite.select(all, 16, 5L));
        assertEquals(16, new HashSet<>(DivanDigSite.select(all, 16, 5L)).size());
        assertEquals(5, DivanDigSite.select(offsets(5), 16, 5L).size());
        assertTrue(DivanDigSite.select(List.of(), 16, 5L).isEmpty());
    }

    @Test
    void differentSeedsPickDifferentSpots() {
        List<Pos> all = offsets(40);
        HashSet<List<Pos>> variants = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) variants.add(DivanDigSite.select(all, 16, seed));
        assertTrue(variants.size() > 1);
    }

    @Test
    void scannedSpotsKeepTheirDistanceAndAreDeterministic() {
        List<Pos> floor = new ArrayList<>();
        for (int x = 0; x < 100; x += 2) for (int z = 0; z < 100; z += 2) floor.add(new Pos(x, 0, z));
        List<Pos> chosen = DivanDigSite.spread(floor, 16, 9L);
        assertEquals(16, chosen.size());
        assertEquals(chosen, DivanDigSite.spread(floor, 16, 9L));
        for (int i = 0; i < chosen.size(); i++) {
            for (int j = i + 1; j < chosen.size(); j++) assertTrue(chosen.get(i).distance(chosen.get(j)) >= 12);
        }
    }

    @Test
    void goldFloorIsReadFromTheSchematicAtTheChosenOrigin() {
        net.minestom.server.instance.block.Block[] palette = {
                net.minestom.server.instance.block.Block.AIR, net.minestom.server.instance.block.Block.GOLD_BLOCK};
        int[] indices = new int[4 * 3 * 4];
        for (int x = 0; x < 4; x++) for (int z = 0; z < 4; z++) indices[x + z * 4 + 0 * 16] = 1;
        net.swofty.type.skyblockgeneric.schematic.Schematic shape =
                net.swofty.type.skyblockgeneric.schematic.Schematic.of(4, 3, 4, palette, indices);
        List<Pos> floor = DivanDigSite.goldFloor(shape, new Pos(100, 50, 200));
        assertEquals(4, floor.size());
        assertTrue(floor.contains(new Pos(100.5, 51, 200.5)));
        assertTrue(floor.contains(new Pos(102.5, 51, 202.5)));
    }
}
