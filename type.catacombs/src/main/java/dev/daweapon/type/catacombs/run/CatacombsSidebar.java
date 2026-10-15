package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.type.catacombs.engine.KeyType;
import dev.daweapon.type.generic.scoreboard.HypixelScoreboard;
import dev.daweapon.type.skyblockgeneric.user.AreaSidebar;
import dev.daweapon.type.skyblockgeneric.user.ChallengeSidebar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** The run's scoreboard: the dungeon and floor on the location line, then time, progress, keys and secrets. */
public final class CatacombsSidebar {
    static final int MAX_LINES = HypixelScoreboard.MAX_LINES - 8;
    private static final String SPACER = " ";

    public record Snapshot(CatacombsLifecycle.Stage stage, int present, int partySize, int readyCount, int waitSecondsLeft,
                           long elapsedSeconds, int clearedPercent, int secretsFound, int totalSecrets, int crypts,
                           int witherKeys, boolean bloodKey, int deaths, int shutdownSecondsLeft) {
    }

    private CatacombsSidebar() {
    }

    public static void register() {
        AreaSidebar.register(CatacombsSidebar::area);
        ChallengeSidebar.register(CatacombsSidebar::lines);
        ChallengeSidebar.hidePurseWhen(CatacombsSidebar::running);
    }

    private static @Nullable Text area(SkyBlockPlayer player) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null) return null;
        return Text.of("<c>The Catacombs <8>({})", run.floorId().encode());
    }

    private static boolean running(SkyBlockPlayer player) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        return run != null && run.started();
    }

    private static @Nullable List<String> lines(SkyBlockPlayer player) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        return run == null ? null : lines(snapshot(run));
    }

    static Snapshot snapshot(CatacombsRun run) {
        CatacombsLifecycle lifecycle = run.lifecycle();
        CatacombsRunState state = lifecycle.state();
        int total = lifecycle.config().totalRooms();
        int cleared = state == null || total == 0 ? 0 : state.completedRooms() * 100 / total;
        long readyCount = run.onlineMembers().stream().filter(member -> run.isReady(member.getUuid())).count();
        return new Snapshot(lifecycle.stage(), run.onlineMembers().size(), run.roster().size(), (int) readyCount,
                run.readyCheck().secondsLeft(), lifecycle.elapsedSeconds(), Math.min(100, cleared),
                state == null ? 0 : state.secretsFound(), lifecycle.config().totalSecrets(), state == null ? 0 : state.crypts(),
                run.keys().count(KeyType.WITHER), run.keys().has(KeyType.BLOOD), state == null ? 0 : state.deaths(),
                lifecycle.shutdownSecondsLeft());
    }

    static List<String> lines(Snapshot snapshot) {
        List<String> lines = new ArrayList<>();
        if (snapshot.stage() == CatacombsLifecycle.Stage.WAITING) {
            lines.add("<f>Party: <a>" + snapshot.present() + "<7>/<a>" + snapshot.partySize());
            lines.add("<f>Ready: <a>" + snapshot.readyCount() + "<7>/<a>" + snapshot.present());
            lines.add("<f>Auto start in: <e>" + snapshot.waitSecondsLeft() + "s");
            return lines;
        }
        lines.add("<f>Time Elapsed: <a>" + elapsed(snapshot.elapsedSeconds()));
        if (snapshot.stage() == CatacombsLifecycle.Stage.ENDING) {
            lines.add("<f>Instance Shutdown In: <a>" + shutdown(snapshot.shutdownSecondsLeft()));
            return lines;
        }
        lines.add("<f>Dungeon Cleared: <e>" + snapshot.clearedPercent() + "%");
        lines.add(SPACER);
        lines.add("<f>Keys: " + keys(snapshot));
        lines.add("<f>Secrets Found: <a>" + snapshot.secretsFound() + "<7>/<a>" + snapshot.totalSecrets());
        lines.add("<f>Crypts: <a>" + snapshot.crypts());
        lines.add("<f>Deaths: <c>" + snapshot.deaths());
        return fit(lines);
    }

    private static String keys(Snapshot snapshot) {
        String wither = snapshot.witherKeys() > 0 ? "<a>✔ <8>Wither" + (snapshot.witherKeys() > 1 ? " x" + snapshot.witherKeys() : "")
                : "<c>✖ <8>Wither";
        String blood = snapshot.bloodKey() ? "<a>✔ <c>Blood" : "<c>✖ <c>Blood";
        return wither + " " + blood;
    }

    private static List<String> fit(List<String> lines) {
        if (lines.size() > MAX_LINES) lines.remove(SPACER);
        return lines.size() > MAX_LINES ? new ArrayList<>(lines.subList(0, MAX_LINES)) : lines;
    }

    static String elapsed(long seconds) {
        return seconds < 60 ? seconds + "s" : seconds / 60 + "m " + String.format("%02ds", seconds % 60);
    }

    private static String shutdown(int seconds) {
        return String.format("%02dm %02ds", seconds / 60, seconds % 60);
    }
}
