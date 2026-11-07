package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The chat at the end of a run, line for line as both captured Floor I runs sent it: the score screen two seconds
 * after the boss's last line, what clicking EXTRA STATS prints, and the re-queue prompt that comes with the first exit
 * gate. Hypixel centres each line with leading spaces, measured in the default font's pixel widths.
 */
public final class ScoreScreen {
    public static final String EXTRA_STATS_COMMAND = "/showextrastats";
    public static final String REQUEUE_COMMAND = "/instancerequeue";
    static final String BAR = "<a><l>" + "▬".repeat(64);
    private static final int CENTRE_PX = 160;
    private static final int SPACE_PX = 4;
    private static final String EXTRA_STATS = "<f>" + " ".repeat(29)
            + "<hover:'<#FFFF55>Click to view extra stats!'><click:run:'" + EXTRA_STATS_COMMAND + "'><6>> <e><l>EXTRA STATS</l> <6>\\<</click></hover>";
    private static final Map<Character, Integer> WIDTHS = widths();

    /** What one member earned, for the two experience lines. */
    public record Earned(long catacombsXp, Map<String, Long> classXp) {
    }

    /** One member's numbers for EXTRA STATS. */
    public record Stats(String className, long damage, long healing, int kills, int deaths, int secrets) {
    }

    private ScoreScreen() {
    }

    public static List<String> won(CatacombsFloor floor, CatacombsMode mode, DungeonScoreBreakdown score, long seconds, Earned earned) {
        List<String> lines = new ArrayList<>();
        lines.add(BAR);
        lines.add(titleLine(title(floor, mode)));
        lines.add("");
        lines.add(scoreLine(score));
        lines.add(defeatedLine(floor, seconds));
        lines.add(EXTRA_STATS);
        lines.addAll(experience(earned));
        lines.add(BAR);
        return lines;
    }

    public static List<String> failed(CatacombsFloor floor, CatacombsMode mode, DungeonScoreBreakdown score, long seconds) {
        List<String> lines = new ArrayList<>();
        lines.add(BAR);
        lines.add(titleLine(title(floor, mode)));
        lines.add("");
        lines.add(scoreLine(score));
        lines.add(centred("<c>☠ <e>The party failed after <a>" + CatacombsMessages.duration(seconds),
                "☠ The party failed after " + CatacombsMessages.duration(seconds)));
        lines.add(BAR);
        return lines;
    }

    /** What clicking EXTRA STATS shows (captured from the auto-run of the same command). */
    public static List<String> extraStats(CatacombsFloor floor, CatacombsMode mode, DungeonScoreBreakdown score, long seconds,
                                          boolean won, Stats stats) {
        List<String> lines = new ArrayList<>();
        lines.add(BAR);
        lines.add(titleLine(title(floor, mode) + " Stats"));
        lines.add("");
        lines.add(scoreLine(score));
        if (won) lines.add(defeatedLine(floor, seconds));
        lines.add("");
        lines.add(stat("Total Damage as " + stats.className() + ": ", "<a>", String.format(Locale.US, "%,d", stats.damage())));
        if (stats.healing() > 0) lines.add(stat("Ally Healing: ", "<a>", String.format(Locale.US, "%,d", stats.healing())));
        lines.add(stat("Enemies Killed: ", "<a>", String.valueOf(stats.kills())));
        lines.add(stat("Deaths: ", "<c>", String.valueOf(stats.deaths())));
        lines.add(stat("Secrets Found: ", "<b>", String.valueOf(stats.secrets())));
        lines.add("");
        lines.add(BAR);
        return lines;
    }

    /** The re-queue block sent with the first exit gate. */
    public static List<String> requeue(CatacombsFloor floor, CatacombsMode mode) {
        String tier = floor == CatacombsFloor.ENTRANCE ? "Entrance" : floor.displayName();
        String instance = mode == CatacombsMode.MASTER ? "Master Mode The Catacombs" : "The Catacombs";
        String hover = "<7>Instance: <#55FF55>" + instance + "\n<#AAAAAA>Tier: <#55FFFF>" + tier;
        return List.of("", BAR, "      <hover:'" + hover + "'><click:run:'" + REQUEUE_COMMAND
                + "'><7>Click <e><l>HERE</l> <7>to re-queue into <a>" + instance + "<7>!</click></hover>", BAR, "");
    }

    private static String title(CatacombsFloor floor, CatacombsMode mode) {
        String name = floor == CatacombsFloor.ENTRANCE ? "Entrance" : floor.displayName();
        return (mode == CatacombsMode.MASTER ? "Master Mode Catacombs" : "The Catacombs") + " - " + name;
    }

    private static String titleLine(String plain) {
        return centred("<c>" + plain.replace(" - ", " <8>- <e>"), plain);
    }

    private static String centred(String markup, String plain) {
        return "<f>" + spaces(plain) + markup;
    }

    private static String scoreLine(DungeonScoreBreakdown score) {
        String colour = rankColour(score.rank());
        String plain = "Team Score: " + score.total() + " (" + score.rank().displayName() + ")";
        return centred("<f>Team Score: " + colour + score.total() + " <f>(" + colour + score.rank().displayName() + "<f>)", plain);
    }

    private static String defeatedLine(CatacombsFloor floor, long seconds) {
        String time = CatacombsMessages.duration(seconds);
        String plain = "☠ Defeated " + floor.bossName() + " in " + time;
        return centred("<c>☠ <e>Defeated <c>" + floor.bossName() + " <e>in <a>" + time, plain);
    }

    private static List<String> experience(Earned earned) {
        List<String> lines = new ArrayList<>();
        lines.add(xpLine(earned.catacombsXp(), "Catacombs Experience"));
        earned.classXp().forEach((name, amount) -> lines.add(xpLine(amount, name + " Experience")));
        return lines;
    }

    private static String xpLine(long amount, String what) {
        String number = String.format(Locale.US, "%,.1f", (double) amount);
        return centred("<8>+<3>" + number + " " + what, "+" + number + " " + what);
    }

    private static String stat(String label, String colour, String value) {
        return centred("<f>" + label + colour + value, label + value);
    }

    static String spaces(String plain) {
        int needed = CENTRE_PX - pixels(plain) / 2;
        StringBuilder out = new StringBuilder();
        for (int done = 0; done < needed; done += SPACE_PX) out.append(' ');
        return out.toString();
    }

    static int pixels(String plain) {
        int total = 0;
        for (char c : plain.toCharArray()) total += WIDTHS.getOrDefault(c, 5) + 1;
        return total;
    }

    private static String rankColour(DungeonScoreRank rank) {
        return switch (rank) {
            case S_PLUS -> "<6>";
            case S -> "<e>";
            case A -> "<a>";
            case B -> "<9>";
            case C -> "<7>";
            case D -> "<c>";
        };
    }

    private static Map<Character, Integer> widths() {
        Map<Character, Integer> widths = new HashMap<>();
        for (char c : "fkt(){}<>".toCharArray()) widths.put(c, 4);
        for (char c : "I[]\"".toCharArray()) widths.put(c, 3);
        for (char c : "il!:;'.,|".toCharArray()) widths.put(c, 1);
        widths.put('@', 6);
        widths.put(' ', 3);
        return Map.copyOf(widths);
    }
}
