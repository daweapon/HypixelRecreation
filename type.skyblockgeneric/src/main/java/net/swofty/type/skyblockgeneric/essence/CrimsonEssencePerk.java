package net.swofty.type.skyblockgeneric.essence;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import org.jetbrains.annotations.Nullable;

/**
 * Vulcan's Crimson Essence Shop perks, from the wiki's Vulcan/UI page and NEU essenceshops.json.
 * The four Kuudra perks can't be bought until the Kuudra fight exists.
 */
@Getter
public enum CrimsonEssencePerk implements EssencePerk {
    STRONGARM("Strongarm", new double[]{10, 25}, new long[]{1_000, 2_000}, KuudraAvailability.UNAVAILABLE_REASON),
    FRESH_TOOLS("Fresh Tools", new double[]{5, 7.5, 10, 12.5, 15},
            new long[]{200, 400, 600, 800, 1_000}, KuudraAvailability.UNAVAILABLE_REASON),
    FUNGUS_FORTUNA("Fungus Fortuna", new double[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20},
            new long[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1_000}, null),
    HARENA_FORTUNA("Harena Fortuna", new double[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20},
            new long[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1_000}, null),
    HEADSTART("Headstart", new double[]{10, 20, 30, 40, 50},
            new long[]{100, 200, 300, 400, 500}, KuudraAvailability.UNAVAILABLE_REASON),
    KUUDRA_MASTER("Kuudra Master", new double[]{10}, new long[]{5_000}, KuudraAvailability.UNAVAILABLE_REASON),
    CRIMSON_TRAINING("Crimson Training", new double[]{3, 5, 10}, new long[]{250, 1_250, 5_000}, null),
    WITHER_PIPER("Wither Piper", new double[]{2, 4, 6, 8, 10},
            new long[]{500, 750, 1_250, 2_000, 3_000}, null);

    private final String displayName;
    private final double[] values;
    private final long[] costs;
    private final @Nullable String unavailableReason;

    CrimsonEssencePerk(String displayName, double[] values, long[] costs, @Nullable String unavailableReason) {
        this.displayName = displayName;
        this.values = values;
        this.costs = costs;
        this.unavailableReason = unavailableReason;
    }

    @Override
    public EssenceType essence() {
        return EssenceType.CRIMSON;
    }
}
