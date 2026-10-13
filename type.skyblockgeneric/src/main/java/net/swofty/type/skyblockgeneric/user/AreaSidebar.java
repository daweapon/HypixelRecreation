package net.swofty.type.skyblockgeneric.user;

import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Replaces the region name on the SkyBlock scoreboard's location line for players in an instanced area
 * (e.g. "Kuudra's Hollow (T5)"). The first provider that answers for a player wins.
 */
public final class AreaSidebar {
    private static final List<Function<SkyBlockPlayer, @Nullable Text>> PROVIDERS = new CopyOnWriteArrayList<>();

    private static final List<Function<SkyBlockPlayer, @Nullable List<Text>>> DETAIL_PROVIDERS = new CopyOnWriteArrayList<>();

    private AreaSidebar() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable Text> provider) {
        PROVIDERS.add(provider);
    }

    public static void registerDetail(Function<SkyBlockPlayer, @Nullable List<Text>> provider) {
        DETAIL_PROVIDERS.add(provider);
    }

    public static List<Text> detailFor(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable List<Text>> provider : DETAIL_PROVIDERS) {
            List<Text> lines = provider.apply(player);
            if (lines != null) return lines;
        }
        return List.of();
    }

    public static @Nullable Text areaFor(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable Text> provider : PROVIDERS) {
            Text area = provider.apply(player);
            if (area != null) return area;
        }
        return null;
    }
}
