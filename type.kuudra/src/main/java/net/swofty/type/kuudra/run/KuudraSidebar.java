package net.swofty.type.kuudra.run;

import net.swofty.commons.text.Text;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.skyblockgeneric.user.AreaSidebar;
import net.swofty.type.skyblockgeneric.user.ChallengeSidebar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** The run's scoreboard: the Hollow and tier on the location line, then the fight's lines (research K1 section 7.1). */
public final class KuudraSidebar {
    /** The SkyBlock sidebar around these lines (date, location block, a spacer and the footer) takes 8 of 15. */
    static final int MAX_LINES = HypixelScoreboard.MAX_LINES - 8;
    private static final String SPACER = " ";

    private KuudraSidebar() {
    }

    public static void register() {
        AreaSidebar.register(KuudraSidebar::area);
        ChallengeSidebar.register(KuudraSidebar::lines);
        ChallengeSidebar.hidePurseWhen(KuudraSidebar::fighting);
    }

    private static @Nullable Text area(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        return run == null ? null : Text.of("<c>Kuudra's Hollow <8>(T{})", run.tier().ordinal() + 1);
    }

    private static boolean fighting(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        return run != null && run.state().stage() != GameState.WAITING;
    }

    private static @Nullable List<String> lines(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        if (run == null) return null;
        KuudraFight fight = run.fight();
        return lines(run.state(), run.onlineMembers().size(), run.roster().size(), fight == null ? null : fight.hud(player));
    }

    static List<String> lines(KuudraRunState state, int present, int partySize) {
        return lines(state, present, partySize, null);
    }

    static List<String> lines(KuudraRunState state, int present, int partySize, KuudraFight.@Nullable Hud hud) {
        List<String> lines = new ArrayList<>();
        if (state.stage() == GameState.WAITING) {
            lines.add("<f>Party: <a>" + present + "<7>/<a>" + partySize);
            lines.add("<e>Talk to Elle to begin!");
            return lines;
        }
        lines.add("<f>Time Elapsed: <a>" + elapsed(state.elapsedSeconds()));
        if (state.stage() == GameState.ENDING) {
            lines.add("<f>Instance Shutdown In: <a>" + shutdown(state.shutdownSecondsLeft()));
            return lines;
        }
        int seconds = state.secondsToNextWave();
        if (hud == null || hud.waves()) {
            lines.add("<f><l>Wave: <c><l>" + state.wave() + (seconds > 0 ? " <8>- <a>" + seconds / 60 + ":" + String.format("%02d", seconds % 60) : ""));
        }
        if (hud != null) {
            lines.add("<f>Tokens: <5>" + hud.tokens());
            if (hud.submergesIn() >= 0) lines.add("<f>Submerges In: <e>" + hud.submergesIn() + "s");
        }
        if (state.phase() != null && (hud == null || hud.objective()) && !state.phase().objective(state.stunRequired()).isEmpty()) {
            lines.add(SPACER);
            List<String> objective = new ArrayList<>(state.phase().objective(state.stunRequired()));
            if (hud != null && hud.progress() != null && !objective.isEmpty()) {
                objective.set(0, objective.getFirst() + " <7>(<a>" + hud.progress() + "<7>)");
            }
            lines.addAll(objective);
        }
        if (hud != null && hud.status() != null) lines.add(hud.status());
        return fit(lines);
    }

    private static List<String> fit(List<String> lines) {
        if (lines.size() > MAX_LINES) lines.remove(SPACER);
        return lines.size() > MAX_LINES ? new ArrayList<>(lines.subList(0, MAX_LINES)) : lines;
    }

    private static String elapsed(long seconds) {
        return seconds < 60 ? seconds + "s" : seconds / 60 + "m " + String.format("%02ds", seconds % 60);
    }

    private static String shutdown(int seconds) {
        return String.format("%02dm %02ds", seconds / 60, seconds % 60);
    }
}
