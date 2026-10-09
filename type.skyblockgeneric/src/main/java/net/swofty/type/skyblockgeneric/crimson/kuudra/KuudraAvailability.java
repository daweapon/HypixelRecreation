package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * The single switch for everything that needs the Kuudra boss fight (Roadmap #11). Until the fight
 * exists, every Kuudra-dependent entry reads its state and wording from here, so turning the fight
 * on later is a one-line change.
 */
public final class KuudraAvailability {
    public static final String UNAVAILABLE_REASON = "Kuudra isn't available yet.";
    public static final String UNAVAILABLE_LORE = "<c>" + UNAVAILABLE_REASON;
    public static final String UNAVAILABLE_CHAT = "<c>Kuudra isn't available yet! <7>The fight is still being built.";

    private KuudraAvailability() {
    }

    public static boolean isFightAvailable() {
        return false;
    }

    /** Staff can queue the fight before it opens to everyone, so it can be tested on a live server. */
    public static boolean canQueue(SkyBlockPlayer player) {
        return isFightAvailable() || player.getRank().isEqualOrHigherThan(Rank.STAFF);
    }

    public static List<String> queueLore(SkyBlockPlayer player) {
        return canQueue(player) ? List.of() : List.of("", UNAVAILABLE_LORE);
    }

    public static List<String> lore() {
        return isFightAvailable() ? List.of() : List.of("", UNAVAILABLE_LORE);
    }

    /** Sends the standard line and returns true when the fight is unavailable, for early-return guards. */
    public static boolean blockIfUnavailable(SkyBlockPlayer player) {
        if (isFightAvailable()) return false;
        player.sendMessage(UNAVAILABLE_CHAT);
        return true;
    }
}
