package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.event.custom.KuudraClearedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Per-profile Kuudra clears (none are possible until the fight exists) and the tier requirements
 * Crag checks. The faction questline owner registers its completion check with
 * {@link #registerQuestlineCheck}; until then, having joined a faction stands in for it.
 */
public final class KuudraProgress {
    private static final String CLEARED = "kuudra:cleared:";
    private static volatile Predicate<SkyBlockPlayer> questlineCheck =
            player -> CrimsonReputationService.faction(player) != null;

    private KuudraProgress() {
    }

    public static void registerQuestlineCheck(Predicate<SkyBlockPlayer> check) {
        questlineCheck = check;
    }

    /** Whether the faction questline is done up to its first-Kuudra step, which is what Crag asks for. */
    public static boolean isQuestlineReady(SkyBlockPlayer player) {
        return questlineCheck.test(player);
    }

    public static boolean hasCleared(SkyBlockPlayer player, KuudraTier tier) {
        return DatapointCrimsonIsle.of(player).getNumber(CLEARED + tier.name()) > 0;
    }

    public static boolean hasDefeatedKuudra(SkyBlockPlayer player) {
        return hasCleared(player, KuudraTier.BASIC);
    }

    public static Set<KuudraTier> cleared(SkyBlockPlayer player) {
        Set<KuudraTier> cleared = EnumSet.noneOf(KuudraTier.class);
        for (KuudraTier tier : KuudraTier.values()) if (hasCleared(player, tier)) cleared.add(tier);
        return cleared;
    }

    /** Records a win. Only the Kuudra fight (Roadmap #11) calls this. */
    public static void recordClear(SkyBlockPlayer player, KuudraTier tier) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().merge(CLEARED + tier.name(), 1L, Long::sum));
        HypixelEventHandler.callCustomEvent(new KuudraClearedEvent(player, tier));
    }

    public static List<Requirement> requirements(SkyBlockPlayer player, KuudraTier tier) {
        List<Requirement> requirements = new ArrayList<>();
        int reputation = CrimsonReputationService.highest(player);
        switch (tier) {
            case BASIC -> requirements.add(new Requirement("Have Elle take you to/Kuudra!", hasDefeatedKuudra(player)));
            case HOT -> requirements.add(new Requirement("Complete the main quest!", isQuestlineReady(player)));
            default -> {
            }
        }
        if (tier != KuudraTier.BASIC) {
            requirements.add(new Requirement(tier.getRequiredReputation() + " Reputation with/Mages or Barbarians.",
                    reputation >= tier.getRequiredReputation()));
            requirements.add(new Requirement("Beat the previous tier.", hasCleared(player, tier.previous())));
        }
        return requirements;
    }

    public static boolean meetsRequirements(SkyBlockPlayer player, KuudraTier tier) {
        return requirements(player, tier).stream().allMatch(Requirement::met);
    }

    /** One wiki requirement line; a "/" marks where Hypixel wraps it in the menu. */
    public record Requirement(String text, boolean met) {
        public List<String> lore() {
            String colour = met ? "<a>" : "<c>";
            String[] parts = text.split("/");
            List<String> lines = new ArrayList<>();
            lines.add(colour + (met ? "✔ " : "✗ ") + parts[0]);
            for (int i = 1; i < parts.length; i++) lines.add(colour + parts[i]);
            return lines;
        }
    }
}
