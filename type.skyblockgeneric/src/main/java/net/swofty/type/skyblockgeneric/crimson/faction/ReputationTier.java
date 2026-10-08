package net.swofty.type.skyblockgeneric.crimson.faction;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum ReputationTier {
    NEUTRAL("Neutral", "<7>", 0, 0, 0),
    FRIENDLY("Friendly", "<a>", 1_000, 5, 0.05),
    TRUSTED("Trusted", "<2>", 3_000, 10, 0.10),
    HONORED("Honored", "<b>", 7_000, 20, 0.15),
    HERO("Hero", "<6>", 12_000, 40, 0.20),
    ;

    private final String displayName;
    private final String colour;
    private final int requiredReputation;
    private final int skyBlockXP;
    private final double emissaryDiscount;

    ReputationTier(String displayName, String colour, int requiredReputation, int skyBlockXP, double emissaryDiscount) {
        this.displayName = displayName;
        this.colour = colour;
        this.requiredReputation = requiredReputation;
        this.skyBlockXP = skyBlockXP;
        this.emissaryDiscount = emissaryDiscount;
    }

    public String coloured() {
        return colour + displayName;
    }

    public boolean isAtLeast(ReputationTier other) {
        return ordinal() >= other.ordinal();
    }

    public @Nullable ReputationTier next() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    public static ReputationTier of(int reputation) {
        ReputationTier result = NEUTRAL;
        for (ReputationTier tier : values()) {
            if (reputation >= tier.requiredReputation) result = tier;
        }
        return result;
    }

    /** The SkyBlock XP a faction has granted in total once its reputation has reached {@code tier}. */
    public static int cumulativeSkyBlockXP(ReputationTier tier) {
        int total = 0;
        for (ReputationTier each : values()) {
            if (each.ordinal() <= tier.ordinal()) total += each.skyBlockXP;
        }
        return total;
    }
}
