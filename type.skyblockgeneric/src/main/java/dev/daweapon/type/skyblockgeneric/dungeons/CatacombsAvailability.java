package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * What of the Catacombs can be entered. Only the floors with a run behind them are playable (Entrance and Floor I);
 * the rest stay locked for everyone. Until {@link CatacombsLaunch} says {@code public: true}, only staff can queue.
 */
public final class CatacombsAvailability {
    public static final Set<CatacombsFloor> PLAYABLE_FLOORS = EnumSet.of(CatacombsFloor.ENTRANCE, CatacombsFloor.FLOOR_ONE);
    public static final String UNAVAILABLE_REASON = "The Catacombs aren't available yet.";
    public static final String UNAVAILABLE_LORE = "<c>" + UNAVAILABLE_REASON;
    public static final String UNAVAILABLE_CHAT = "<c>The Catacombs aren't available yet! <7>The dungeon is still being built.";
    public static final String FLOOR_LOCKED_LORE = "<c>This floor isn't available yet!";
    public static final String FLOOR_LOCKED_CHAT = "<c>This floor isn't available yet! <7>Only Entrance and Floor I can be entered for now.";

    private static volatile CatacombsLaunch launch = new CatacombsLaunch(CatacombsLaunch.FILE, System::currentTimeMillis);

    private CatacombsAvailability() {
    }

    public static void use(CatacombsLaunch source) {
        launch = source;
    }

    public static boolean isPublic() {
        return launch.isPublic();
    }

    public static boolean isPlayable(CatacombsFloor floor) {
        return PLAYABLE_FLOORS.contains(floor);
    }

    public static boolean canQueue(boolean staff, CatacombsFloor floor) {
        return isPlayable(floor) && (isPublic() || staff);
    }

    public static boolean canQueue(SkyBlockPlayer player, CatacombsFloor floor) {
        return canQueue(isStaff(player), floor);
    }

    public static boolean canUsePartyFinder(SkyBlockPlayer player) {
        return isPublic() || isStaff(player);
    }

    public static List<String> floorLore(SkyBlockPlayer player, CatacombsFloor floor) {
        if (!isPlayable(floor)) return List.of("", FLOOR_LOCKED_LORE);
        return canQueue(player, floor) ? List.of() : List.of("", UNAVAILABLE_LORE);
    }

    public static boolean blockIfUnavailable(SkyBlockPlayer player, CatacombsFloor floor) {
        if (!isPlayable(floor)) {
            player.sendMessage(FLOOR_LOCKED_CHAT);
            return true;
        }
        if (!canQueue(player, floor)) {
            player.sendMessage(UNAVAILABLE_CHAT);
            return true;
        }
        return false;
    }

    private static boolean isStaff(SkyBlockPlayer player) {
        return player.getRank().isEqualOrHigherThan(Rank.STAFF);
    }
}
