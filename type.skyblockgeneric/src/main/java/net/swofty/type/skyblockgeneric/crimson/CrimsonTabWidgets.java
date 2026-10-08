package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The Crimson Isle's tab-list widgets. Each widget's lines (header first) come from a provider
 * that the owning system replaces with {@link #provide}; the defaults cover systems not built yet.
 */
public final class CrimsonTabWidgets {
    public enum Widget {
        REPUTATION, FACTION_QUESTS, TROPHY_FISH, VOLCANO
    }

    private static final Map<Widget, Function<SkyBlockPlayer, List<String>>> PROVIDERS = new EnumMap<>(Widget.class);

    static {
        provide(Widget.REPUTATION, CrimsonTabWidgets::reputation);
        provide(Widget.FACTION_QUESTS, player -> List.of("<e><l>Faction Quests:", " <7>Not available yet."));
        provide(Widget.TROPHY_FISH, player -> List.of("<6><l>Trophy Fish:", " <7>Not available yet."));
        provide(Widget.VOLCANO, player -> List.of(volcano()));
    }

    private CrimsonTabWidgets() {
    }

    public static void provide(Widget widget, Function<SkyBlockPlayer, List<String>> provider) {
        PROVIDERS.put(widget, provider);
    }

    public static List<String> lines(Widget widget, SkyBlockPlayer player) {
        return PROVIDERS.get(widget).apply(player);
    }

    private static List<String> reputation(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return List.of("<a><l>Reputation:", " <7>Join a faction through <d>Elle<7>.");

        int reputation = CrimsonReputationService.get(player, faction);
        ReputationTier tier = ReputationTier.of(reputation);
        ReputationTier next = tier.next();
        List<String> lines = new ArrayList<>();
        lines.add(faction.getColour() + "<l>" + faction.getDisplayName() + " Reputation:");
        lines.add(" " + tier.coloured() + " <7>" + StringUtility.commaify(reputation)
                + (next == null ? "" : "<8>/" + StringUtility.commaify(next.getRequiredReputation())));
        return lines;
    }

    private static String volcano() {
        VolcanoExplosivity explosivity = VolcanoEruptionService.currentExplosivity();
        return "<c><l>Volcano: " + (explosivity == null ? "<7>INACTIVE" : explosivity.coloured());
    }
}
