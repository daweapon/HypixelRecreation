package net.swofty.type.skyblockgeneric.user;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Sidebar lines for a timed activity the player is in (e.g. a Dojo test). The first provider that answers for a
 * player wins; its lines replace the objective block on the SkyBlock scoreboard.
 */
public final class ChallengeSidebar {
    private static final List<Function<SkyBlockPlayer, @Nullable List<String>>> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<Predicate<SkyBlockPlayer>> PURSE_HIDERS = new CopyOnWriteArrayList<>();

    private ChallengeSidebar() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable List<String>> provider) {
        PROVIDERS.add(provider);
    }

    /** Activities whose lines take the purse's place too, as Kuudra's fight does on Hypixel. */
    public static void hidePurseWhen(Predicate<SkyBlockPlayer> condition) {
        PURSE_HIDERS.add(condition);
    }

    public static boolean hidesPurse(SkyBlockPlayer player) {
        return PURSE_HIDERS.stream().anyMatch(condition -> condition.test(player));
    }

    public static @Nullable List<String> linesFor(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable List<String>> provider : PROVIDERS) {
            List<String> lines = provider.apply(player);
            if (lines != null) return lines;
        }
        return null;
    }
}
