package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Kuudra boss collection (wiki Kuudra#Collection): a clear adds its tier's points (Basic 1 up to
 * Infernal 5), and each tier pays an item, Crimson Essence, Kuudra Teeth and SkyBlock XP once.
 */
@Getter
public enum KuudraCollectionTier {
    I(10, 10, "KUUDRA_PET", 400, 1, List.of("<f>Kuudra Pet", "<d>400 Crimson Essence", "<5>Kuudra Teeth")),
    II(100, 15, "UNCOMMON_KUUDRA_CHUNK", 1_000, 2, List.of("<a>Kuudra Chunk", "<d>1,000 Crimson Essence", "<5>2x Kuudra Teeth")),
    III(500, 20, "RARE_KUUDRA_CHUNK", 2_500, 4, List.of("<9>Meaty Kuudra Chunk", "<d>2,500 Crimson Essence", "<5>4x Kuudra Teeth")),
    IV(2_000, 25, "EPIC_KUUDRA_CHUNK", 10_000, 8, List.of("<5>Hardened Kuudra Chunk", "<d>10,000 Crimson Essence", "<5>8x Kuudra Teeth")),
    V(5_000, 30, "LEGENDARY_KUUDRA_CHUNK", 20_000, 12, List.of("<6>Enriched Kuudra Chunk", "<d>20,000 Crimson Essence", "<5>12x Kuudra Teeth")),
    ;

    private final int requiredProgress;
    private final int skyBlockXP;
    private final String itemId;
    private final int essence;
    private final int teeth;
    private final List<String> rewards;

    KuudraCollectionTier(int requiredProgress, int skyBlockXP, String itemId, int essence, int teeth, List<String> rewards) {
        this.requiredProgress = requiredProgress;
        this.skyBlockXP = skyBlockXP;
        this.itemId = itemId;
        this.essence = essence;
        this.teeth = teeth;
        this.rewards = rewards;
    }

    /** Collection points one clear of a tier is worth: Basic 1 up to Infernal 5 (Hot to Fiery are linear estimates). */
    public static int pointsFor(KuudraTier tier) {
        return tier.ordinal() + 1;
    }

    /** How many tiers the given points have reached. */
    public static int reachedCount(long points) {
        int reached = 0;
        for (KuudraCollectionTier tier : values()) if (points >= tier.requiredProgress) reached++;
        return reached;
    }

    public static @Nullable KuudraCollectionTier highestReached(long points) {
        int reached = reachedCount(points);
        return reached == 0 ? null : values()[reached - 1];
    }

    public static int skyBlockXP(long points) {
        int xp = 0;
        for (KuudraCollectionTier tier : values()) if (points >= tier.requiredProgress) xp += tier.skyBlockXP;
        return xp;
    }
}
