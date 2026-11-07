package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The dungeon tab list in Hypixel's four 20-row columns: the party with classes, the run's player stats and
 * discoveries, the dungeon's rooms, secrets, time and puzzles, and the party's Dungeon Buffs.
 */
public final class CatacombsTabLines {
    public static final int ROWS = 20;
    public static final int COLUMNS = 4;
    private static final int SLOTS_PER_MEMBER = 4;
    private static final DecimalFormat PERCENT = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public record Member(int skyBlockLevel, String colour, String name, String className, int classLevel, boolean dead) {
    }

    public record Puzzle(String name, DungeonRoomState state) {
    }

    public record Snapshot(List<Member> party, List<String> downed, int secondsToRevive, int deaths, int secretsFound,
                           int totalSecrets, int crypts, int openedRooms, int completedRooms, long elapsedSeconds,
                           List<Puzzle> puzzles, Map<BlessingType, Integer> blessings) {
    }

    private CatacombsTabLines() {
    }

    /** All 80 slots, column by column; null slots are blank. */
    public static List<@Nullable String> slots(Snapshot snapshot) {
        String[] slots = new String[ROWS * COLUMNS];
        party(slots, snapshot);
        playerStats(slots, snapshot);
        dungeon(slots, snapshot);
        buffs(slots, snapshot);
        return Arrays.asList(slots);
    }

    private static void party(String[] slots, Snapshot snapshot) {
        slots[0] = "<b><l>Party</l> <f>(" + snapshot.party().size() + ")";
        for (int index = 0; index < snapshot.party().size() && 1 + index * SLOTS_PER_MEMBER < ROWS; index++) {
            slots[1 + index * SLOTS_PER_MEMBER] = member(snapshot.party().get(index));
        }
    }

    public static int memberAt(int slot) {
        if (slot <= 0 || slot >= ROWS || (slot - 1) % SLOTS_PER_MEMBER != 0) return -1;
        return (slot - 1) / SLOTS_PER_MEMBER;
    }

    static String member(Member member) {
        String state = member.dead() ? "<c>DEAD" : "<e>" + member.className() + " " + classLevel(member.classLevel());
        return "<8>[<7>" + member.skyBlockLevel() + "<8>] <color:" + member.colour() + ">" + member.name() + " <f>(" + state + "<f>)";
    }

    private static String classLevel(int level) {
        return level <= 0 ? "0" : StringUtility.getAsRomanNumeral(level);
    }

    private static void playerStats(String[] slots, Snapshot snapshot) {
        int base = ROWS;
        slots[base] = "<b><l>Player Stats";
        boolean anyDowned = !snapshot.downed().isEmpty();
        slots[base + 1] = "<f>Downed: " + (anyDowned ? "<e>" + String.join(", ", snapshot.downed()) : "<a>NONE");
        slots[base + 2] = "<f>Time: " + (anyDowned && snapshot.secondsToRevive() > 0 ? "<e>" + snapshot.secondsToRevive() + "s" : "<7>N/A");
        slots[base + 3] = "<f>Revive: <7>N/A";
        slots[base + 5] = "<f>Team Deaths: <c>" + snapshot.deaths();
        slots[base + 10] = "<b><l>Discoveries: <f>" + (snapshot.secretsFound() + snapshot.crypts());
        slots[base + 11] = " <f>Secrets Found: <b>" + snapshot.secretsFound();
        slots[base + 12] = " <f>Crypts: <6>" + snapshot.crypts();
    }

    private static void dungeon(String[] slots, Snapshot snapshot) {
        int base = ROWS * 2;
        slots[base] = "<b><l>Dungeon: <7>Catacombs";
        slots[base + 1] = " <f>Opened Rooms: <5>" + snapshot.openedRooms();
        slots[base + 2] = " <f>Completed Rooms: <5>" + snapshot.completedRooms();
        slots[base + 3] = " <f>Secrets Found: <e>" + secretPercent(snapshot.secretsFound(), snapshot.totalSecrets()) + "%";
        slots[base + 4] = " <f>Time: <6>" + time(snapshot.elapsedSeconds());
        slots[base + 6] = "<b><l>Puzzles: <f>(" + snapshot.puzzles().size() + ")";
        for (int index = 0; index < snapshot.puzzles().size() && base + 7 + index < base + ROWS; index++) {
            slots[base + 7 + index] = puzzle(snapshot.puzzles().get(index));
        }
    }

    static String secretPercent(int found, int total) {
        return total <= 0 ? "0" : PERCENT.format(found * 100.0 / total);
    }

    static String time(long seconds) {
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        String text = (minutes > 0 || hours > 0 ? minutes + "m " : "") + seconds % 60 + "s";
        return hours > 0 ? hours + "h " + text : text;
    }

    static String puzzle(Puzzle puzzle) {
        String mark = switch (puzzle.state()) {
            case COMPLETED, CLEARED -> "<a>✔";
            case FAILED -> "<c>✖";
            default -> "<6>✦";
        };
        String name = puzzle.state() == DungeonRoomState.UNDISCOVERED ? "<7>???" : "<d>" + puzzle.name();
        return " " + name + ": <f>[" + mark + "<f>]";
    }

    private static void buffs(String[] slots, Snapshot snapshot) {
        int base = ROWS * 3;
        slots[base] = "<b><l>Dungeon Buffs";
        List<String> lines = new ArrayList<>();
        snapshot.blessings().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> lines.add("<f>" + entry.getKey().displayName() + " <a>" + StringUtility.getAsRomanNumeral(entry.getValue())));
        if (lines.isEmpty()) lines.addAll(List.of("<7>No Buffs active. Find them", "<7>by exploring the Dungeon!"));
        for (int index = 0; index < lines.size() && index + 1 < ROWS; index++) slots[base + 1 + index] = lines.get(index);
    }
}
