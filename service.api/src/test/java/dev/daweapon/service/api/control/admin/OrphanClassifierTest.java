package dev.daweapon.service.api.control.admin;

import dev.daweapon.service.api.control.admin.OrphanClassifier.CoopFacts;
import dev.daweapon.service.api.control.admin.OrphanClassifier.Inputs;
import dev.daweapon.service.api.control.admin.OrphanClassifier.Orphan;
import dev.daweapon.service.api.control.admin.OrphanClassifier.ProfileFacts;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrphanClassifierTest {
    private static Inputs inputs(Map<String, ProfileFacts> profiles, Map<String, String> owners, Set<String> islands,
                                 Set<String> linked, List<CoopFacts> coops) {
        return new Inputs(profiles, owners, Set.of("alice", "bob"), islands, linked, coops);
    }

    private static Map<String, ProfileFacts> profiles(ProfileFacts... facts) {
        Map<String, ProfileFacts> map = new LinkedHashMap<>();
        for (ProfileFacts fact : facts) map.put(fact.profileId(), fact);
        return map;
    }

    @Test
    void healthyDataHasNoOrphans() {
        Inputs inputs = inputs(profiles(new ProfileFacts("p1", "alice", null, 10), new ProfileFacts("p2", "bob", "i2", 5)),
                Map.of("p1", "alice", "p2", "bob"), Set.of("p1", "i2", "c1"), Set.of("c1"),
                List.of(new CoopFacts("coop", List.of("p1", "p2"))));
        assertEquals(List.of(), OrphanClassifier.classify(inputs));
        assertEquals(15.0, OrphanClassifier.totalCoins(inputs));
    }

    @Test
    void flagsAProfileNoAccountListsWithTheReason() {
        Inputs inputs = inputs(profiles(new ProfileFacts("p1", "alice", null, 0), new ProfileFacts("p2", "ghost", null, 0),
                        new ProfileFacts("p3", null, null, 0)),
                Map.of(), Set.of(), Set.of(), List.of());
        List<Orphan> orphans = OrphanClassifier.classify(inputs);
        assertEquals(3, orphans.size());
        assertEquals("owner's account does not list this profile", orphans.get(0).reason());
        assertEquals("owner account is missing", orphans.get(1).reason());
        assertNull(orphans.get(2).related());
    }

    @Test
    void anIslandIsOrphanedOnlyWhenNoRouteLeadsToIt() {
        Inputs inputs = inputs(profiles(new ProfileFacts("p1", "alice", "doc-island", 0)), Map.of("p1", "alice"),
                Set.of("p1", "doc-island", "coop-island", "lost"), Set.of("coop-island"), List.of());
        List<Orphan> orphans = OrphanClassifier.classify(inputs);
        assertEquals(1, orphans.size());
        assertEquals(new Orphan(OrphanClassifier.ISLAND, "lost", "no profile or co-op points at this island", null), orphans.getFirst());
    }

    @Test
    void flagsCoopsPointingAtMissingProfiles() {
        Inputs inputs = inputs(profiles(new ProfileFacts("p1", "alice", null, 0)), Map.of("p1", "alice"), Set.of(), Set.of(),
                List.of(new CoopFacts("c1", List.of("p1", "gone1", "gone2"))));
        List<Orphan> orphans = OrphanClassifier.classify(inputs);
        assertEquals(1, orphans.size());
        assertEquals(OrphanClassifier.COOP, orphans.getFirst().kind());
        assertEquals("gone1,gone2", orphans.getFirst().related());
        assertTrue(orphans.getFirst().reason().contains("2 missing"));
    }

    @Test
    void unquoteAndNumberReadDatapointSerialisations() {
        assertEquals("abc", OrphanScan.unquote("\"abc\""));
        assertNull(OrphanScan.unquote("null"));
        assertNull(OrphanScan.unquote(null));
        assertEquals(1234.5, OrphanScan.number("1234.5"));
        assertEquals(0.0, OrphanScan.number("not a number"));
    }
}
