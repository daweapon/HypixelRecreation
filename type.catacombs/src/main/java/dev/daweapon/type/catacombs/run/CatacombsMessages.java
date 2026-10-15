package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;

import java.util.ArrayList;
import java.util.List;

/** Every line the run says to the party, kept in one place. */
public final class CatacombsMessages {
    private static final String BAR = "<8>" + "▬".repeat(40);
    private static final String WATCHER = "<c>[BOSS] The Watcher<f>: ";

    private CatacombsMessages() {
    }

    public static String title(CatacombsFloor floor, CatacombsMode mode) {
        String name = floor == CatacombsFloor.ENTRANCE ? "Entrance" : floor.displayName();
        return (mode == CatacombsMode.MASTER ? "<c>Master Mode " : "<c>") + "The Catacombs <8>- <e>" + name;
    }

    public static List<String> dungeonEntered(CatacombsFloor floor, CatacombsMode mode) {
        return List.of(BAR, "<e>Dungeon entered<7>: " + title(floor, mode), BAR);
    }

    public static String startingIn(int seconds) {
        return "<a>Dungeon starts in " + seconds + (seconds == 1 ? " second." : " seconds.");
    }

    public static String ready(String player) {
        return "<a>" + player + " <7>is now ready!";
    }

    public static String notReady(String player) {
        return "<c>" + player + " <7>is no longer ready.";
    }

    public static String classPlaceholder(String className) {
        return "<7>Your class is <e>" + className + "<7>. Class selection is coming soon.";
    }

    public static String bloodRoomWithoutKey() {
        return "<c>The party entered the Blood Room without opening its door with the Blood Key!";
    }

    public static String watcherIntro() {
        return WATCHER + "Things feel a little more roomy now, eh?";
    }

    public static String watcherDone() {
        return WATCHER + "That will be enough for now.";
    }

    public static String watcherEntranceDone() {
        return WATCHER + "Congratulations, you made it through the Entrance.";
    }

    public static String portalOpened() {
        return "<5>A portal to the boss room has opened!";
    }

    public static String bossIntro(CatacombsFloor floor) {
        return "<c>[BOSS] " + floor.bossName() + "<f>: Gratz for making it this far, but I'm basically unbeatable.";
    }

    public static String bossSecondPhase(CatacombsFloor floor) {
        return "<c>[BOSS] " + floor.bossName() + "<f>: Oh I'm dead!!!";
    }

    public static String bossDefeated(CatacombsFloor floor) {
        return "<c>[BOSS] " + floor.bossName() + "<f>: I'll be back.";
    }

    public static String ghost(int seconds) {
        return "<c> ☠ You died and became a ghost." + (seconds > 0 ? " <7>You will be revived in " + seconds + " seconds." : "");
    }

    public static String revived() {
        return "<a>You have been revived!";
    }

    public static String died(String player) {
        return "<c> ☠ <a>" + player + "<7> died and became a ghost.";
    }

    public static String disconnected(String player) {
        return "<c> ☠ <a>" + player + "<7> disconnected from the Dungeon and became a ghost.";
    }

    public static List<String> summary(CatacombsFloor floor, CatacombsMode mode, boolean win, CatacombsRunState state,
                                       DungeonScoreBreakdown score) {
        List<String> lines = new ArrayList<>();
        lines.add(BAR);
        lines.add(title(floor, mode));
        lines.add("<f>    Team Score: <a>" + score.total() + " <7>(" + rankColour(score.rank()) + score.rank().displayName() + "<7>)");
        lines.add(win ? "<f>    ☠ Defeated <c>" + floor.bossName() + "<f> in <a>" + duration(state.elapsed().toSeconds())
                : "<c>The run failed after <a>" + duration(state.elapsed().toSeconds()));
        lines.add("<f>Skill: <a>" + score.skill() + " <7>(" + state.deaths() + " deaths, " + state.failedPuzzles() + " failed puzzles)");
        lines.add("<f>Exploration: <a>" + score.exploration() + " <7>(" + state.completedRooms() + "/" + state.config().totalRooms()
                + " rooms, " + state.secretsFound() + "/" + state.config().totalSecrets() + " secrets)");
        lines.add("<f>Speed: <a>" + score.speed());
        lines.add("<f>Bonus: <a>" + score.bonus() + " <7>(" + state.crypts() + " crypts)");
        if (score.leavePenalty() > 0) lines.add("<c>Leave penalty: <a>-" + score.leavePenalty());
        lines.add(BAR);
        return lines;
    }

    public static String returning(int seconds) {
        return "<7>Returning to the Dungeon Hub in <a>" + seconds + " seconds<7>.";
    }

    public static String duration(long seconds) {
        return String.format("%02dm %02ds", seconds / 60, seconds % 60);
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
}
