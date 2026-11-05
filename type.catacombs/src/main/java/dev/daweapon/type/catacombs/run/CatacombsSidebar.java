package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.type.catacombs.engine.KeyType;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.scoreboard.HypixelScoreboard;
import dev.daweapon.type.skyblockgeneric.user.AreaSidebar;
import dev.daweapon.type.skyblockgeneric.user.ChallengeSidebar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The run's scoreboard as Hypixel lays it out: keys, time, cleared percent with the live score, then the teammates. */
public final class CatacombsSidebar {
    static final int MAX_LINES = HypixelScoreboard.MAX_LINES - 8;
    private static final String SPACER = " ";

    public record Teammate(char classLetter, String colour, String name, int health, int maxHealth, boolean dead) {
    }

    public record Snapshot(CatacombsLifecycle.Stage stage, int present, int partySize, int readyCount, int waitSecondsLeft,
                           long elapsedSeconds, int clearedPercent, int score, int witherKeys, boolean bloodKey,
                           List<Teammate> teammates, int shutdownSecondsLeft) {
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
        return run == null ? null : lines(snapshot(run, player.getUuid()));
    }

    static Snapshot snapshot(CatacombsRun run, UUID viewer) {
        CatacombsLifecycle lifecycle = run.lifecycle();
        CatacombsRunState state = lifecycle.state();
        int total = lifecycle.config().totalRooms();
        int cleared = state == null || total == 0 ? 0 : state.completedRooms() * 100 / total;
        long readyCount = run.onlineMembers().stream().filter(member -> run.isReady(member.getUuid())).count();
        List<Teammate> teammates = run.onlineMembers().stream()
                .filter(member -> !member.getUuid().equals(viewer))
                .map(member -> teammate(run, member))
                .toList();
        return new Snapshot(lifecycle.stage(), run.onlineMembers().size(), run.roster().size(), (int) readyCount,
                run.readyCheck().secondsLeft(), lifecycle.elapsedSeconds(), Math.min(100, cleared),
                state == null ? 0 : state.score().total(), run.keys().count(KeyType.WITHER), run.keys().has(KeyType.BLOOD),
                teammates, lifecycle.shutdownSecondsLeft());
    }

    private static Teammate teammate(CatacombsRun run, SkyBlockPlayer member) {
        var rank = member.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();
        var dungeonClass = run.classOf(member.getUuid());
        char letter = dungeonClass == null ? '?' : dungeonClass.displayName().charAt(0);
        return new Teammate(letter, rank.getTextColor().asHexString(), member.getUsername(),
                (int) Math.ceil(member.getHealth()), (int) Math.ceil(member.getMaxHealth()), run.ghosts().isGhost(member.getUuid()));
    }

    static List<String> lines(Snapshot snapshot) {
        List<String> lines = new ArrayList<>();
        if (snapshot.stage() == CatacombsLifecycle.Stage.WAITING) {
            lines.add("<f>Party: <a>" + snapshot.present() + "<7>/<a>" + snapshot.partySize());
            lines.add("<f>Ready: <a>" + snapshot.readyCount() + "<7>/<a>" + snapshot.present());
            lines.add("<f>Auto start in: <e>" + snapshot.waitSecondsLeft() + "s");
            return lines;
        }
        if (snapshot.stage() == CatacombsLifecycle.Stage.ENDING) {
            lines.add("<f>Time Elapsed: <a>" + elapsed(snapshot.elapsedSeconds()));
            lines.add("<f>Instance Shutdown In: <a>" + shutdown(snapshot.shutdownSecondsLeft()));
            return lines;
        }
        lines.add("<f>Keys: " + keys(snapshot));
        lines.add("<f>Time Elapsed: <a>" + elapsed(snapshot.elapsedSeconds()));
        lines.add("<f>Cleared: " + clearedColour(snapshot.clearedPercent()) + snapshot.clearedPercent() + "% <8>(" + snapshot.score() + ")");
        if (!snapshot.teammates().isEmpty()) lines.add(SPACER);
        snapshot.teammates().forEach(teammate -> lines.add(teammate(teammate)));
        return fit(lines);
    }

    private static String keys(Snapshot snapshot) {
        return "<c>■ " + (snapshot.bloodKey() ? "<a>✔" : "<c>✖") + " <8>■ <a>" + snapshot.witherKeys() + "x";
    }

    static String clearedColour(int percent) {
        if (percent >= 100) return "<a>";
        if (percent >= 60) return "<e>";
        if (percent >= 30) return "<6>";
        return "<c>";
    }

    static String teammate(Teammate teammate) {
        String status = teammate.dead() ? "<c>DEAD" : healthColour(teammate) + StringUtility.commaify(teammate.health()) + "<c>❤";
        return "<e>[" + teammate.classLetter() + "] <color:" + teammate.colour() + ">" + teammate.name() + " " + status;
    }

    private static String healthColour(Teammate teammate) {
        double fraction = teammate.maxHealth() <= 0 ? 1 : (double) teammate.health() / teammate.maxHealth();
        if (fraction >= 0.5) return "<a>";
        return fraction >= 0.25 ? "<e>" : "<c>";
    }

    private static List<String> fit(List<String> lines) {
        if (lines.size() > MAX_LINES) lines.remove(SPACER);
        return lines.size() > MAX_LINES ? new ArrayList<>(lines.subList(0, MAX_LINES)) : lines;
    }

    static String elapsed(long seconds) {
        return seconds < 60 ? seconds + "s" : String.format("%02dm %02ds", seconds / 60, seconds % 60);
    }

    private static String shutdown(int seconds) {
        return String.format("%02dm %02ds", seconds / 60, seconds % 60);
    }
}
