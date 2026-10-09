package net.swofty.type.skyblockgeneric.user;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Activities with their own death rules, such as a Kuudra run's ghosts and revives. The first override that
 * returns true handled the death, and the default SkyBlock death is skipped.
 */
public final class PlayerDeathOverrides {
    private static final List<Predicate<SkyBlockPlayer>> OVERRIDES = new CopyOnWriteArrayList<>();

    private PlayerDeathOverrides() {
    }

    public static void register(Predicate<SkyBlockPlayer> override) {
        OVERRIDES.add(override);
    }

    public static boolean handle(SkyBlockPlayer player) {
        for (Predicate<SkyBlockPlayer> override : OVERRIDES) {
            if (override.test(player)) return true;
        }
        return false;
    }
}
