package net.swofty.type.skyblockgeneric.crimson.kuudra.reward;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KuudraChestStorageTest {
    private static final long HOUR = Duration.ofHours(1).toMillis();

    private static KuudraRun run(long completedAt) {
        return KuudraRun.create(KuudraTier.BURNING, completedAt,
                List.of(KuudraReward.essence(25)),
                List.of(KuudraReward.item("MANDRAA", 1), KuudraReward.book("STRONG_MANA", 3), KuudraReward.essence(400)));
    }

    @Test
    void keepsTheNewestSixtyRuns() {
        List<KuudraRun> runs = new ArrayList<>();
        long now = 100 * HOUR;
        for (int i = 0; i < 70; i++) runs = KuudraChestStorage.withRun(runs, run(now - 60 * HOUR + i * 1000L), now);
        assertEquals(KuudraChestStorage.MAX_RUNS, runs.size());
        assertEquals(now - 60 * HOUR + 69 * 1000L, runs.getFirst().completedAt());
        assertEquals(now - 60 * HOUR + 10 * 1000L, runs.getLast().completedAt());
    }

    @Test
    void runsExpireAfterSeventyTwoHours() {
        long now = 1_000 * HOUR;
        List<KuudraRun> runs = List.of(run(now - 73 * HOUR), run(now - 71 * HOUR));
        assertEquals(1, KuudraChestStorage.alive(runs, now).size());
        assertEquals(now - 71 * HOUR, KuudraChestStorage.alive(runs, now).getFirst().completedAt());
    }

    @Test
    void survivesTheSaveFormat() {
        KuudraRun opened = run(5 * HOUR).open(KuudraChest.PAID);
        List<KuudraRun> parsed = KuudraChestStorage.parse(KuudraChestStorage.serialize(List.of(opened, run(6 * HOUR))));
        assertEquals(opened, parsed.getFirst());
        assertNull(parsed.get(1).opened());
        assertEquals(List.of(), KuudraChestStorage.parse(null));
    }
}
