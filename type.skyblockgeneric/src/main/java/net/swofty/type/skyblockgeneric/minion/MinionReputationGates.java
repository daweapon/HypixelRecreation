package net.swofty.type.skyblockgeneric.minion;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.crimson.CrimsonGates;
import net.swofty.type.skyblockgeneric.crimson.CrimsonGates.Faction;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Crimson Isle minions need faction reputation for every tier.
 */
public final class MinionReputationGates {
    private static final int[] REQUIRED = {500, 1_000, 1_500, 2_000, 3_000, 4_000, 5_500, 6_500, 7_500, 9_000, 10_000, 12_000};
    private static final Map<MinionRegistry, Faction> FACTIONS = Map.of(
            MinionRegistry.MYCELIUM, Faction.MAGE,
            MinionRegistry.RED_SAND, Faction.BARBARIAN);

    private MinionReputationGates() {
    }

    public static @Nullable Faction factionFor(MinionRegistry minion) {
        return FACTIONS.get(minion);
    }

    public static int requiredReputation(int tier) {
        return REQUIRED[Math.max(0, Math.min(REQUIRED.length, tier) - 1)];
    }

    public static @Nullable String requirementLine(MinionRegistry minion, int tier) {
        Faction faction = factionFor(minion);
        if (faction == null) return null;
        return "<4>❣ <c>Requires " + StringUtility.commaify(requiredReputation(tier)) + " " + faction.displayName() + " Reputation.";
    }

    public static SkyBlockRecipe.CraftingResult check(SkyBlockPlayer player, MinionRegistry minion, int tier) {
        Faction faction = factionFor(minion);
        if (faction == null || CrimsonGates.hasReputation(player, faction, requiredReputation(tier))) {
            return new SkyBlockRecipe.CraftingResult(true, null);
        }
        return new SkyBlockRecipe.CraftingResult(false, new String[]{requirementLine(minion, tier)});
    }
}
