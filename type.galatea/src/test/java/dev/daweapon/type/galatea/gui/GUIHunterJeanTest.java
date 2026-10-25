package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.skyblockgeneric.fishing.torrhus.TorrhusSeaCreatures;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GUIHunterJeanTest {
    @Test
    void weightsCoverTenThousand() {
        assertEquals(10_000, GUIHunterJean.ENTRIES.stream().mapToInt(GUIHunterJean.Entry::weight).sum());
    }

    @Test
    void everyEntryHasAProfileAndAUniqueSlot() {
        List<String> ids = TorrhusSeaCreatures.PROFILES.stream().map(p -> p.id()).toList();
        assertEquals(ids.size(), GUIHunterJean.ENTRIES.size());
        assertEquals(GUIHunterJean.ENTRIES.size(), new HashSet<>(GUIHunterJean.ENTRIES.stream().map(GUIHunterJean.Entry::slot).toList()).size());
        GUIHunterJean.ENTRIES.forEach(entry -> assertTrue(ids.contains(entry.id())));
    }

    @Test
    void spawnChanceFormatsLikeTheWiki() {
        String isopod = GUIHunterJean.describe(entry("GIANT_ISOPOD"), TorrhusSeaCreatures.PROFILES.get(5));
        assertTrue(isopod.contains("<a>0.25% <8>(25 Weight)"));
        String haggard = GUIHunterJean.describe(entry("HAGGARD"), TorrhusSeaCreatures.PROFILES.get(0));
        assertTrue(haggard.contains("<a>50% <8>(5000 Weight)"));
        assertTrue(haggard.startsWith("<7>[Lvl 44] Haggard"));
    }

    private static GUIHunterJean.Entry entry(String id) {
        return GUIHunterJean.ENTRIES.stream().filter(e -> e.id().equals(id)).findFirst().orElseThrow();
    }
}
