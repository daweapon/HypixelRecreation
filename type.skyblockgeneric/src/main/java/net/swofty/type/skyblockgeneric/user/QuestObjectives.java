package net.swofty.type.skyblockgeneric.user;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Quest systems that put an objective on the SkyBlock sidebar (e.g. the Crimson Isle questlines).
 * The first provider with an objective for the player wins.
 */
public final class QuestObjectives {
    private static final List<Function<SkyBlockPlayer, @Nullable QuestObjective>> PROVIDERS = new CopyOnWriteArrayList<>();

    private QuestObjectives() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable QuestObjective> provider) {
        PROVIDERS.add(provider);
    }

    public static @Nullable QuestObjective current(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable QuestObjective> provider : PROVIDERS) {
            try {
                QuestObjective objective = provider.apply(player);
                if (objective != null) return objective;
            } catch (RuntimeException exception) {
                Logger.error(exception, "Quest objective provider failed for {}", player.getUsername());
            }
        }
        return null;
    }
}
