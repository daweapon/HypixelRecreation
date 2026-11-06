package dev.daweapon.type.catacombs.engine.watcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Watcher's Floor I rhythm from the two captured runs. */
class WatcherWavesTest {
    private static final int FLOOR_ONE = 9;
    private static WatcherConfig config;

    @BeforeAll
    static void load() {
        config = WatcherConfig.load(Path.of("..", "configuration", "skyblock", "dungeons", "watcher.json"));
    }

    private record Run(WatcherWaves waves, List<Long> summons, List<String> lines, List<Long> portal, List<Long> bar) {
    }

    private static Run play(long seed, int killDelay) {
        WatcherWaves waves = new WatcherWaves(config, FLOOR_ONE, new Random(seed));
        Run run = new Run(waves, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<Long> dueKills = new ArrayList<>();
        for (int tick = 0; tick < 20 * 120 && run.portal().isEmpty(); tick++) {
            for (WatcherWaves.Action action : waves.tick()) record(run, action, dueKills, killDelay);
            while (!dueKills.isEmpty() && dueKills.getFirst() <= waves.now()) {
                dueKills.removeFirst();
                for (WatcherWaves.Action action : waves.killed()) record(run, action, dueKills, killDelay);
                assertTrue(waves.alive() >= 0);
            }
            assertTrue(waves.alive() <= config.firstWave(), "never more than four summons alive");
        }
        return run;
    }

    private static void record(Run run, WatcherWaves.Action action, List<Long> dueKills, int killDelay) {
        long now = run.waves().now();
        switch (action) {
            case WatcherWaves.Summon() -> {
                run.summons().add(now);
                dueKills.add(now + killDelay);
                dueKills.sort(Long::compare);
            }
            case WatcherWaves.Say(String line) -> run.lines().add(line);
            case WatcherWaves.OpenPortal() -> run.portal().add(now);
            case WatcherWaves.ShowBar() -> run.bar().add(now);
            case WatcherWaves.FlyOut() -> {
            }
        }
    }

    @Test
    void theFirstWaveOfFourComesOnTheCapturedBeat() {
        Run run = play(1, 20 * 60);
        assertEquals(List.of(200L, 246L, 304L, 364L), run.summons().subList(0, 4));
        assertTrue(run.summons().get(4) > 364 + 20 * 60, "the fifth waits for the first wave to die");
        assertEquals("Ah, we meet again. As I foresaw...", run.lines().getFirst());
        assertEquals(List.of(40L), run.bar());
    }

    @Test
    void nothingMoreSpawnsUntilTheFirstWaveIsDeadThenTheRestFollow() {
        int killDelay = 20 * 8;
        Run run = play(2, killDelay);
        assertEquals(FLOOR_ONE, run.summons().size());
        long firstWaveDead = run.summons().get(3) + killDelay;
        long fifth = run.summons().get(4);
        assertTrue(fifth > firstWaveDead, "fifth summon at " + fifth + " before the wave died at " + firstWaveDead);
        assertTrue(fifth - firstWaveDead <= config.lineToSpawnTicks() + config.laterGapMaxTicks());
    }

    @Test
    void itSaysEnoughThenProvenThenOpensThePortal() {
        Run run = play(3, 20);
        List<String> lines = run.lines();
        int enough = lines.indexOf(config.enoughLine());
        int proven = lines.indexOf(config.provenLine());
        assertTrue(enough > 0 && proven > enough, lines.toString());
        assertEquals(proven, lines.size() - 1);
        assertEquals(1, run.portal().size());
        assertTrue(run.waves().cleared());
        assertEquals(0, run.waves().remaining());
    }

    @Test
    void theBarCountsTheSummonsStillToKillAndFlipsItsName() {
        WatcherWaves waves = new WatcherWaves(config, FLOOR_ONE, new Random(4));
        assertEquals(1f, waves.progress());
        List<String> titles = new ArrayList<>();
        for (int tick = 0; tick < 300; tick++) {
            for (WatcherWaves.Action action : waves.tick()) {
                if (action instanceof WatcherWaves.Summon && waves.spawned() == 1) waves.killed();
            }
            if (tick % 20 == 0) titles.add(waves.barTitle("The Watcher"));
        }
        assertEquals(8f / 9f, waves.progress(), 1e-6);
        assertTrue(titles.contains("The Watcher"));
        assertTrue(titles.contains("The Watcher 9/9"));
        assertTrue(titles.contains("The Watcher 8/9"));
        assertFalse(waves.finished());
    }
}
