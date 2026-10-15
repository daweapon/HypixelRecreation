package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.type.skyblockgeneric.dungeons.queue.CatacombsPartyCheck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsPartyCheckTest {
    private static CatacombsPartyCheck.Member ready(String name, boolean self) {
        return new CatacombsPartyCheck.Member(name, self, true, true);
    }

    private static List<CatacombsPartyCheck.Member> party(int size) {
        List<CatacombsPartyCheck.Member> members = new ArrayList<>();
        members.add(ready("Leader", true));
        for (int i = 1; i < size; i++) members.add(ready("P" + i, false));
        return members;
    }

    @Test
    void aQualifiedSoloPlayerOrFullPartyMayQueue() {
        assertTrue(CatacombsPartyCheck.problems(true, party(1), "Floor I").isEmpty());
        assertTrue(CatacombsPartyCheck.problems(true, party(5), "Floor I").isEmpty());
    }

    @Test
    void onlyTheLeaderMayQueue() {
        assertEquals(List.of("<c>Only your party leader can queue for The Catacombs!"),
                CatacombsPartyCheck.problems(false, party(2), "Floor I"));
    }

    @Test
    void partiesAreCappedAtFive() {
        assertEquals(List.of("<c>Catacombs parties can have at most 5 players! Yours has 6."),
                CatacombsPartyCheck.problems(true, party(6), "Floor I"));
    }

    @Test
    void everyMemberMustBeHereAndQualified() {
        List<String> problems = CatacombsPartyCheck.problems(true, List.of(
                new CatacombsPartyCheck.Member("Leader", true, true, false),
                new CatacombsPartyCheck.Member("Away", false, false, false),
                new CatacombsPartyCheck.Member("Fresh", false, true, false),
                ready("Ready", false)), "Floor I");
        assertEquals(List.of(
                "<c>You don't meet the requirements for Floor I!",
                "<c>Away must be on your server to queue! <7>Use <e>/p warp <7>first.",
                "<c>Fresh doesn't meet the requirements for Floor I!"), problems);
    }

    @Test
    void anEmptyRosterIsRefused() {
        assertEquals(List.of("<c>There is nobody to queue!"), CatacombsPartyCheck.problems(true, List.of(), "Entrance"));
    }
}
