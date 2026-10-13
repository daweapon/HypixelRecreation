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
}
