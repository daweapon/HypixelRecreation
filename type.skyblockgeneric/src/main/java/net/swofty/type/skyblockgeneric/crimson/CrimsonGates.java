package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

/**
 * Adapter between item/minion/essence content and the faction and Kuudra systems. Until those
 * systems are installed through the setters, reputation gates are not enforced and Kuudra counts
 * as unavailable.
 */
public final class CrimsonGates {
    public enum Faction {
        MAGE("Mage"),
        BARBARIAN("Barbarian");

        private final String displayName;

        Faction(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    @FunctionalInterface
    public interface ReputationSource {
        OptionalInt reputation(SkyBlockPlayer player, Faction faction);
    }

    private static ReputationSource reputationSource = (_, _) -> OptionalInt.empty();
    private static BooleanSupplier kuudraAvailable = () -> false;

    private CrimsonGates() {
    }

    public static void useReputationSource(ReputationSource source) {
        reputationSource = source;
    }

    public static void useKuudraAvailability(BooleanSupplier available) {
        kuudraAvailable = available;
    }

    public static boolean isReputationEnforced(SkyBlockPlayer player) {
        return reputationSource.reputation(player, Faction.MAGE).isPresent();
    }

    public static boolean hasReputation(SkyBlockPlayer player, Faction faction, int required) {
        OptionalInt reputation = reputationSource.reputation(player, faction);
        return reputation.isEmpty() || reputation.getAsInt() >= required;
    }

    public static boolean isKuudraAvailable() {
        return kuudraAvailable.getAsBoolean();
    }
}
