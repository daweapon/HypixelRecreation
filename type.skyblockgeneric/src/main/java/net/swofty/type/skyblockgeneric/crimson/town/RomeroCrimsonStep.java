package net.swofty.type.skyblockgeneric.crimson.town;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Step 4 of the Romero and Juliette quest, which takes place at the Crimson Isle Stronghold. The quest
 * itself (Juliette in The Park and Romero's other nine stops) is not built yet, so the step never
 * becomes active; the quest registers both hooks when it is.
 */
public final class RomeroCrimsonStep {
    private static volatile Predicate<SkyBlockPlayer> active = _ -> false;
    private static volatile Consumer<SkyBlockPlayer> completed = _ -> {
    };

    private RomeroCrimsonStep() {
    }

    public static void register(Predicate<SkyBlockPlayer> isActive, Consumer<SkyBlockPlayer> onCompleted) {
        active = isActive;
        completed = onCompleted;
    }

    public static boolean isActive(SkyBlockPlayer player) {
        return active.test(player);
    }

    public static void complete(SkyBlockPlayer player) {
        completed.accept(player);
    }
}
