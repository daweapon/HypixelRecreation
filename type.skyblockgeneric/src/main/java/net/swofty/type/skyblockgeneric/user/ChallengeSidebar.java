package net.swofty.type.skyblockgeneric.user;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Sidebar lines for a timed activity the player is in (e.g. a Dojo test). The first provider that answers for a
 * player wins; its lines replace the objective block on the SkyBlock scoreboard.
 */
public final class ChallengeSidebar {
    private static final List<Function<SkyBlockPlayer, @Nullable List<String>>> PROVIDERS = new CopyOnWriteArrayList<>();

    private ChallengeSidebar() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable List<String>> provider) {
        PROVIDERS.add(provider);
    }

    public static @Nullable List<String> linesFor(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable List<String>> provider : PROVIDERS) {
            List<String> lines = provider.apply(player);
            if (lines != null) return lines;
        }
        return null;
    }
}
