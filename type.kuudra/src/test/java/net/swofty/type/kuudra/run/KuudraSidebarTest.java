package net.swofty.type.kuudra.run;

import net.swofty.type.kuudra.fight.KuudraFight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraSidebarTest {

    @Test
    void waitingShowsThePartyAndElle() {
        KuudraRunState state = new KuudraRunState(false, false, 35, 100, 20);
        assertEquals(List.of("<f>Party: <a>1<7>/<a>3", "<e>Talk to Elle to begin!"), KuudraSidebar.lines(state, 1, 3));
    }

    @Test
    void fightShowsTimeWaveCountdownAndObjective() {
        KuudraRunState state = new KuudraRunState(true, false, 25, 100, 20);
        state.start();
        for (int i = 0; i < 16; i++) state.tick();
        assertEquals(List.of(
                "<f>Time Elapsed: <a>16s",
                "<f><l>Wave: <c><l>1 <8>- <a>0:09",
                " ",
                "<e>Rescue supplies"), KuudraSidebar.lines(state, 2, 2));
    }

    @Test
    void theStunTiersShowTheCapturedObjectiveAndTheSubmergeTimer() {
        KuudraRunState state = new KuudraRunState(true, true, 15, 100, 20);
        state.start();
        state.enterPhase(KuudraPhase.FUEL);
        assertEquals(List.of(
                "<f>Time Elapsed: <a>0s",
                "<f><l>Wave: <c><l>1 <8>- <a>0:15",
                "<f>Tokens: <5>1192",
                "<f>Submerges In: <e>59s",
                " ",
                "<e>Figure out how to",
                "<e>defeat Kuudra"), KuudraSidebar.lines(state, 4, 4, new KuudraFight.Hud(1192, null, 59, null)));
    }

    @Test
    void theInfernalSkipDropsTheWaveAndObjectiveLines() {
        KuudraRunState state = new KuudraRunState(true, true, 15, 100, 20);
        state.start();
        state.enterPhase(KuudraPhase.LAIR);
        assertEquals(List.of("<f>Time Elapsed: <a>0s", "<f>Tokens: <5>1619"),
                KuudraSidebar.lines(state, 4, 4, new KuudraFight.Hud(1619, null, -1, null, false, false)));
    }

    @Test
    void endingShowsTheShutdownTimer() {
        KuudraRunState state = new KuudraRunState(false, false, 35, 100, 20);
        state.start();
        for (int i = 0; i < 75; i++) state.tick();
        state.finish(KuudraRunState.Outcome.WIN);
        state.tick();
        assertEquals(List.of("<f>Time Elapsed: <a>1m 15s", "<f>Instance Shutdown In: <a>00m 19s"),
                KuudraSidebar.lines(state, 4, 4));
    }

    /** The SkyBlock sidebar around the run's lines: date, blank + 3 location lines, a spacer and the 2-line footer. */
    private static final int SKYBLOCK_LINES = 8;

    private static void fits(List<String> lines, String phase) {
        assertTrue(lines.size() <= KuudraSidebar.MAX_LINES, phase + " has " + lines.size() + " lines");
        assertTrue(SKYBLOCK_LINES + lines.size() <= 15, phase + " overflows the 15-line sidebar");
    }

    private static KuudraFight.Hud busiest() {
        return new KuudraFight.Hud(99_999, "3/4", 59, "<c>FINAL KILLED", true, true);
    }

    @Test
    void waitingFitsWithThePurseShown() {
        KuudraRunState state = new KuudraRunState(true, true, 15, 100, 20);
        List<String> lines = KuudraSidebar.lines(state, 4, 4);
        assertTrue(SKYBLOCK_LINES + 3 + lines.size() <= 15);
    }

    @Test
    void everyPhaseFitsOnEveryTier() {
        for (boolean stun : new boolean[]{false, true}) {
            for (KuudraPhase phase : KuudraPhase.values()) {
                if (!phase.availableIn(stun, stun)) continue;
                KuudraRunState state = new KuudraRunState(stun, stun, 15, 100, 20);
                state.start();
                state.enterPhase(phase);
                fits(KuudraSidebar.lines(state, 4, 4, busiest()), phase + (stun ? " (stun tier)" : ""));
                fits(KuudraSidebar.lines(state, 4, 4), phase + " before the fight hooks in");
            }
        }
    }

    @Test
    void aShownDeathDropsOnlyTheSpacer() {
        KuudraRunState state = new KuudraRunState(true, true, 15, 100, 20);
        state.start();
        state.enterPhase(KuudraPhase.FUEL);
        assertEquals(List.of(
                "<f>Time Elapsed: <a>0s",
                "<f><l>Wave: <c><l>1 <8>- <a>0:15",
                "<f>Tokens: <5>99999",
                "<f>Submerges In: <e>59s",
                "<e>Figure out how to <7>(<a>3/4<7>)",
                "<e>defeat Kuudra",
                "<c>FINAL KILLED"), KuudraSidebar.lines(state, 4, 4, busiest()));
    }

    @Test
    void endingFits() {
        KuudraRunState state = new KuudraRunState(true, true, 15, 100, 20);
        state.start();
        state.finish(KuudraRunState.Outcome.LOSE);
        state.tick();
        fits(KuudraSidebar.lines(state, 4, 4, busiest()), "ENDING");
    }
}
