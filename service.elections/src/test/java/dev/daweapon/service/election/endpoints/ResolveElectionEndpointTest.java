package dev.daweapon.service.election.endpoints;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResolveElectionEndpointTest {
    private final ResolveElectionEndpoint endpoint = new ResolveElectionEndpoint();

    private static Map<String, Object> candidate(String name, List<String> perks) {
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("mayorName", name);
        candidate.put("activePerks", new ArrayList<>(perks));
        return candidate;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> activePerks(Map<String, Object> data) {
        return (Map<String, Object>) data.get("candidateActivePerks");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> failedFlags(Map<String, Object> data) {
        return (Map<String, Object>) data.get("failedPerkGainLastTime");
    }

    @Test
    void wireColorFollowsCandidateIndexOrder() {
        assertEquals(Text.colorTag(NamedTextColor.RED), ResolveElectionEndpoint.wireColorForIndex(0));
        assertEquals(Text.colorTag(NamedTextColor.GREEN), ResolveElectionEndpoint.wireColorForIndex(1));
        assertEquals(Text.colorTag(NamedTextColor.AQUA), ResolveElectionEndpoint.wireColorForIndex(2));
        assertEquals(Text.colorTag(NamedTextColor.YELLOW), ResolveElectionEndpoint.wireColorForIndex(3));
        assertEquals(Text.colorTag(NamedTextColor.LIGHT_PURPLE), ResolveElectionEndpoint.wireColorForIndex(4));
    }

    @Test
    void wireColorFallsBackToWhiteOutOfRange() {
        String white = Text.colorTag(NamedTextColor.WHITE);
        assertEquals(white, ResolveElectionEndpoint.wireColorForIndex(-1));
        assertEquals(white, ResolveElectionEndpoint.wireColorForIndex(5));
    }

    @Test
    void winnerStoredPerksAreReplacedByCandidatePerkAndFailureFlagCleared() {
        Map<String, Object> data = new HashMap<>();
        data.put("candidateActivePerks", new HashMap<>(Map.of("COLE", List.of("PROSPECTION", "MINING_XP_BUFF"))));
        data.put("failedPerkGainLastTime", new HashMap<>(Map.of("COLE", true)));
        List<Map<String, Object>> candidates = List.of(candidate("COLE", List.of("PROSPECTION")));

        endpoint.updatePerkProgression(data, candidates, "COLE");

        assertTrue(activePerks(data).containsKey("COLE"));
        assertEquals(List.of("PROSPECTION"), activePerks(data).get("COLE"));
        assertFalse(failedFlags(data).containsKey("COLE"));
    }

    @Test
    void loserThatFailedLastTimeGainsExactlyOnePerkFromItsPool() {
        Map<String, Object> data = new HashMap<>();
        data.put("failedPerkGainLastTime", new HashMap<>(Map.of("AATROX", true)));
        List<Map<String, Object>> candidates = List.of(
                candidate("AATROX", List.of("SLASHED_PRICING")),
                candidate("DIANA", List.of("PET_XP_BUFF")));

        endpoint.updatePerkProgression(data, candidates, "DIANA");

        @SuppressWarnings("unchecked")
        List<String> perks = (List<String>) activePerks(data).get("AATROX");
        assertEquals(2, perks.size());
        assertTrue(perks.contains("SLASHED_PRICING"));
        assertTrue(List.of("SLAYER_XP_BUFF", "PATHFINDER").containsAll(perks.stream().filter(p -> !p.equals("SLASHED_PRICING")).toList()));
        assertEquals(false, failedFlags(data).get("AATROX"));
    }

    @Test
    void loserOutcomeIsAlwaysConsistentWithFailureFlag() {
        for (int i = 0; i < 30; i++) {
            Map<String, Object> data = new HashMap<>();
            List<Map<String, Object>> candidates = List.of(
                    candidate("PAUL", List.of("MARAUDER")),
                    candidate("DIAZ", List.of("STOCK_EXCHANGE")));

            endpoint.updatePerkProgression(data, candidates, "DIAZ");

            @SuppressWarnings("unchecked")
            List<String> perks = (List<String>) activePerks(data).get("PAUL");
            boolean gained = perks.size() == 2;
            assertTrue(perks.size() == 1 || gained);
            assertEquals(!gained, failedFlags(data).get("PAUL"));
        }
    }

    @Test
    void retiredPerksAreDroppedFromLosers() {
        Map<String, Object> data = new HashMap<>();
        data.put("failedPerkGainLastTime", new HashMap<>(Map.of("AATROX", true)));
        List<Map<String, Object>> candidates = List.of(
                candidate("AATROX", List.of("RETIRED_PERK", "PATHFINDER")),
                candidate("DIANA", List.of("PET_XP_BUFF")));

        endpoint.updatePerkProgression(data, candidates, "DIANA");

        @SuppressWarnings("unchecked")
        List<String> perks = (List<String>) activePerks(data).get("AATROX");
        assertFalse(perks.contains("RETIRED_PERK"));
        assertTrue(perks.contains("PATHFINDER"));
        assertEquals(2, perks.size());
    }

    @Test
    void loserWithEveryPerkAlreadyUnlockedIsUnchangedAndNotFlagged() {
        Map<String, Object> data = new HashMap<>();
        List<String> all = List.of("SLASHED_PRICING", "SLAYER_XP_BUFF", "PATHFINDER");
        List<Map<String, Object>> candidates = List.of(
                candidate("AATROX", all),
                candidate("DIANA", List.of("PET_XP_BUFF")));

        endpoint.updatePerkProgression(data, candidates, "DIANA");

        assertEquals(all, activePerks(data).get("AATROX"));
        assertFalse(failedFlags(data).containsKey("AATROX"));
    }

    @Test
    void specialMayorsDoNotProgressPerksWhenTheyLose() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> candidates = List.of(
                candidate("JERRY", List.of()),
                candidate("COLE", List.of("PROSPECTION")));

        endpoint.updatePerkProgression(data, candidates, "COLE");

        assertFalse(activePerks(data).containsKey("JERRY"));
        assertFalse(failedFlags(data).containsKey("JERRY"));
    }

    @Test
    void regularWinnerWithNoStoredPerksGetsOneOfItsCandidatePerks() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> candidates = List.of(candidate("COLE", List.of("MOLTEN_FORGE")));

        endpoint.updatePerkProgression(data, candidates, "COLE");

        assertEquals(List.of("MOLTEN_FORGE"), activePerks(data).get("COLE"));
    }

    @Test
    void specialWinnerGetsNoStoredPerks() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> candidates = List.of(candidate("SCORPIUS", List.of("SOMETHING")));

        endpoint.updatePerkProgression(data, candidates, "SCORPIUS");

        assertFalse(activePerks(data).containsKey("SCORPIUS"));
    }
}
