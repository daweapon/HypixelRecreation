package net.swofty.type.skyblockgeneric.crimson.kuudra;

import lombok.Getter;

import java.util.List;

/**
 * The Kuudra boss collection (wiki Kuudra#Collection). Progress scales with the tier completed
 * (Basic 1 to Infernal 5 points per clear), so it stays at 0 until the fight exists.
 */
@Getter
public enum KuudraCollectionTier {
    I(10, 10, List.of("<f>Kuudra Pet", "<d>400 Crimson Essence", "<5>Kuudra Teeth")),
    II(100, 15, List.of("<a>Kuudra Chunk", "<d>1,000 Crimson Essence", "<5>2x Kuudra Teeth")),
    III(500, 20, List.of("<9>Meaty Kuudra Chunk", "<d>2,500 Crimson Essence", "<5>4x Kuudra Teeth")),
    IV(2_000, 25, List.of("<5>Hardened Kuudra Chunk", "<d>10,000 Crimson Essence", "<5>8x Kuudra Teeth")),
    V(5_000, 30, List.of("<6>Enriched Kuudra Chunk", "<d>20,000 Crimson Essence", "<5>12x Kuudra Teeth")),
    ;

    private final int requiredProgress;
    private final int skyBlockXP;
    private final List<String> rewards;

    KuudraCollectionTier(int requiredProgress, int skyBlockXP, List<String> rewards) {
        this.requiredProgress = requiredProgress;
        this.skyBlockXP = skyBlockXP;
        this.rewards = rewards;
    }

    /** Collection points one clear of a tier is worth: Basic 1 up to Infernal 5 (Hot to Fiery are linear estimates). */
    public static int pointsFor(KuudraTier tier) {
        return tier.ordinal() + 1;
    }
}
