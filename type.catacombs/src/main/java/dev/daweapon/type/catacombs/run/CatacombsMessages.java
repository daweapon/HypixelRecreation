package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import net.kyori.adventure.text.Component;

import java.util.List;

/** Every line the run says to the party, kept in one place. */
public final class CatacombsMessages {
    private static final String BAR = "<8>" + "▬".repeat(40);
    private static final String WATCHER = "<c>[BOSS] The Watcher<f>: ";
    private static final String MORT = "<e>[NPC] <b>Mort<f>: ";

    private CatacombsMessages() {
    }

    public static String title(CatacombsFloor floor, CatacombsMode mode) {
        String name = floor == CatacombsFloor.ENTRANCE ? "Entrance" : floor.displayName();
        return (mode == CatacombsMode.MASTER ? "<c>Master Mode " : "<c>") + "The Catacombs <8>- <e>" + name;
    }

    public static List<String> dungeonEntered(CatacombsFloor floor, CatacombsMode mode) {
        return List.of(BAR, "<e>Dungeon entered<7>: " + title(floor, mode), BAR);
    }

    /** The countdown once everyone is ready, as in all six captured runs. */
    public static String startingIn(int seconds) {
        return "<a>Starting in " + seconds + (seconds == 1 ? " second." : " seconds.");
    }

    /** {@code §6daweapon§a is now ready!}: the name in its rank colour. */
    public static Text ready(Component name) {
        return Text.of("{}<a> is now ready!", name);
    }

    public static Text notReady(Component name) {
        return Text.of("{}<c> is no longer ready!", name);
    }

    public static String bloodRoomWithoutKey() {
        return "<c>The party entered the Blood Room without opening its door with the Blood Key!";
    }

    public static String watcher(String line) {
        return WATCHER + line;
    }

    public static String watcherEntranceDone() {
        return WATCHER + "Congratulations, you made it through the Entrance.";
    }

    /** Mort's three lines at the start of a run, about two seconds apart in both captured runs. */
    public static List<String> mortLines() {
        return List.of(
                MORT + "Here, I found this map when I first entered the dungeon.",
                MORT + "You should find it useful if you get lost.",
                MORT + "Good luck.");
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

    public static String returning(int seconds) {
        return "<7>Returning to the Dungeon Hub in <a>" + seconds + " seconds<7>.";
    }

    public static String duration(long seconds) {
        return String.format("%02dm %02ds", seconds / 60, seconds % 60);
    }
}
