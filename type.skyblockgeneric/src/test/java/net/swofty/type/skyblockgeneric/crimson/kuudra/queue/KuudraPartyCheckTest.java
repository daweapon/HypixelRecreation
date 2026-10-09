package net.swofty.type.skyblockgeneric.crimson.kuudra.queue;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraPartyCheckTest {

    private static KuudraPartyCheck.Member ready(String name, boolean self) {
        return new KuudraPartyCheck.Member(name, self, true, true);
    }

    @Test
    void aQualifiedSoloPlayerOrFullPartyMayQueue() {
        assertTrue(KuudraPartyCheck.problems(true, List.of(ready("Leader", true)), "Basic").isEmpty());
        assertTrue(KuudraPartyCheck.problems(true, List.of(ready("Leader", true), ready("A", false),
                ready("B", false), ready("C", false)), "Hot").isEmpty());
    }

    @Test
    void onlyTheLeaderMayQueue() {
        assertEquals(List.of("<c>Only your party leader can queue for Kuudra!"),
                KuudraPartyCheck.problems(false, List.of(ready("Leader", false), ready("Me", true)), "Basic"));
    }

    @Test
    void partiesAreCappedAtFour() {
        List<KuudraPartyCheck.Member> five = List.of(ready("Leader", true), ready("A", false), ready("B", false),
                ready("C", false), ready("D", false));
        assertEquals(List.of("<c>Kuudra parties can have at most 4 players! Yours has 5."),
                KuudraPartyCheck.problems(true, five, "Basic"));
    }

    @Test
    void everyMemberMustBeHereAndQualified() {
        List<String> problems = KuudraPartyCheck.problems(true, List.of(
                new KuudraPartyCheck.Member("Leader", true, true, false),
                new KuudraPartyCheck.Member("Away", false, false, false),
                new KuudraPartyCheck.Member("Fresh", false, true, false),
                ready("Ready", false)), "Burning");
        assertEquals(List.of(
                "<c>You don't meet the requirements for the Burning Tier!",
                "<c>Away must be on your server to queue! <7>Use <e>/p warp <7>first.",
                "<c>Fresh doesn't meet the requirements for the Burning Tier!"), problems);
    }
}
