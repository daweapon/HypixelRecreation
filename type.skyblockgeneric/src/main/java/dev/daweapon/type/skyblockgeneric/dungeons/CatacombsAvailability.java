package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * What of the Catacombs can be entered. Only the floors with a run behind them are playable (Entrance and Floor I);
 * the rest stay locked for everyone. Until {@link CatacombsLaunch} says {@code public: true}, only staff, YouTube and MVP++
 * players can queue.
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

    public static final Rank EARLY_ACCESS_RANK = Rank.MVP_PLUS_PLUS;

    public static boolean canQueue(boolean earlyAccess, CatacombsFloor floor) {
        return isPlayable(floor) && (isPublic() || earlyAccess);
    }

    public static boolean canQueue(SkyBlockPlayer player, CatacombsFloor floor) {
        return canQueue(hasEarlyAccess(player.getRank()), floor);
    }

    public static boolean canUsePartyFinder(SkyBlockPlayer player) {
        return isPublic() || hasEarlyAccess(player.getRank());
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

    public static boolean hasEarlyAccess(Rank rank) {
        return rank.isEqualOrHigherThan(EARLY_ACCESS_RANK);
    }
}
