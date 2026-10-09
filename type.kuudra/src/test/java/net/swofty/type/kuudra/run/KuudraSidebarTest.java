package net.swofty.type.kuudra.run;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void endingShowsTheShutdownTimer() {
        KuudraRunState state = new KuudraRunState(false, false, 35, 100, 20);
        state.start();
        for (int i = 0; i < 75; i++) state.tick();
        state.finish(KuudraRunState.Outcome.WIN);
        state.tick();
        assertEquals(List.of("<f>Time Elapsed: <a>1m 15s", "<f>Instance Shutdown In: <a>00m 19s"),
                KuudraSidebar.lines(state, 4, 4));
    }
}
