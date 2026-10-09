package net.swofty.type.kuudra.run;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraQueue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraRequeueTest {
    @Test
    void onlyTheInstanceCreatorCanRequeue() {
        UUID creator = UUID.randomUUID();
        assertEquals(KuudraRequeue.Decision.REQUEUE, KuudraRequeue.decide(creator, creator));
        assertEquals(KuudraRequeue.Decision.NOT_CREATOR, KuudraRequeue.decide(creator, UUID.randomUUID()));
        assertEquals(KuudraRequeue.Decision.NOT_IN_INSTANCE, KuudraRequeue.decide(null, creator));
    }

    @Test
    void theRefusalIsTheCapturedLine() {
        assertEquals("Only the instance creator can re-queue!", Text.parse(KuudraRequeue.NOT_CREATOR).plain());
    }

    @Test
    void thePromptRunsTheRequeueCommandForTheSameTier() {
        String prompt = KuudraRequeue.prompt(KuudraTier.BURNING);
        assertTrue(prompt.contains("<click:run:'/instancerequeue'>"));
        assertEquals("Click here to re-queue for Kuudra's Hollow - Burning Tier!", Text.parse(prompt).plain());
    }

    @Test
    void theQueueingLeaderBecomesTheInstanceCreator() {
        UUID leader = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<UUID> roster = KuudraQueue.leaderFirst(leader, List.of(a, leader, b));
        assertEquals(List.of(leader, a, b), roster);
        assertEquals(List.of(leader), KuudraQueue.leaderFirst(leader, List.of(leader)));
    }

    @Test
    void everyQueuePrintsTheCapturedEntryLine() {
        List<String> lines = KuudraQueue.entered("alum05", KuudraTier.INFERNAL);
        assertEquals(3, lines.size());
        assertEquals("alum05 entered Kuudra's Hollow, Infernal Tier!", Text.parse(lines.get(1)).plain());
        assertEquals("-----------------------------", Text.parse(lines.getFirst()).plain());
    }

    @Test
    void theReadyCheckCountsDownOnceEveryoneIsReady() {
        KuudraReadyCheck check = new KuudraReadyCheck();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertTrue(check.ready(a));
        assertFalse(check.ready(a), "readying twice says nothing");
        assertFalse(check.startCountdown(List.of(a, b)));
        assertTrue(check.ready(b));
        assertTrue(check.startCountdown(List.of(a, b)));
        assertFalse(check.startCountdown(List.of(a, b)), "the countdown starts once");
        assertFalse(check.ready(UUID.randomUUID()), "nobody readies up after the countdown");
        assertEquals(List.of("Starting in 4 seconds.", "Starting in 3 seconds.", "Starting in 2 seconds.", "Starting in 1 second."),
                KuudraReadyCheck.countdown().stream().map(line -> Text.parse(line).plain()).toList());
    }

    @Test
    void aSoloPlayerStartsAsSoonAsTheyAreReady() {
        KuudraReadyCheck check = new KuudraReadyCheck();
        UUID solo = UUID.randomUUID();
        assertFalse(check.startCountdown(List.of()));
        check.ready(solo);
        assertTrue(check.startCountdown(List.of(solo)));
    }
}
