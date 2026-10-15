package dev.daweapon.type.skyblockgeneric.essence;

import lombok.AccessLevel;
import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import org.jetbrains.annotations.Nullable;

/**
 * Vulcan's Crimson Essence Shop perks, from the wiki's Vulcan/UI page and NEU essenceshops.json.
 * The four Kuudra perks follow the Kuudra launch switch.
 */
@Getter
public enum CrimsonEssencePerk implements EssencePerk {
    STRONGARM("Strongarm", new double[]{10, 25}, new long[]{1_000, 2_000}, true),
    FRESH_TOOLS("Fresh Tools", new double[]{5, 7.5, 10, 12.5, 15},
            new long[]{200, 400, 600, 800, 1_000}, true),
    FUNGUS_FORTUNA("Fungus Fortuna", new double[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20},
            new long[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1_000}, false),
    HARENA_FORTUNA("Harena Fortuna", new double[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20},
            new long[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1_000}, false),
    HEADSTART("Headstart", new double[]{10, 20, 30, 40, 50},
            new long[]{100, 200, 300, 400, 500}, true),
    KUUDRA_MASTER("Kuudra Master", new double[]{10}, new long[]{5_000}, true),
    CRIMSON_TRAINING("Crimson Training", new double[]{3, 5, 10}, new long[]{250, 1_250, 5_000}, false),
    WITHER_PIPER("Wither Piper", new double[]{2, 4, 6, 8, 10},
            new long[]{500, 750, 1_250, 2_000, 3_000}, false);

    private final String displayName;
    private final double[] values;
    private final long[] costs;
    @Getter(AccessLevel.NONE)
    private final boolean needsKuudra;

    CrimsonEssencePerk(String displayName, double[] values, long[] costs, boolean needsKuudra) {
        this.displayName = displayName;
        this.values = values;
        this.costs = costs;
        this.needsKuudra = needsKuudra;
    }

    @Override
    public @Nullable String getUnavailableReason() {
        return needsKuudra && !KuudraAvailability.isFightAvailable() ? KuudraAvailability.UNAVAILABLE_REASON : null;
    }

    @Override
    public EssenceType essence() {
        return EssenceType.CRIMSON;
    }
}
