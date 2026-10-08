package net.swofty.type.skyblockgeneric.fishing.trophy;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import net.swofty.type.skyblockgeneric.fishing.catches.TrophyTier;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Odger's four Trophy Fisher ranks (wiki: Odger). Novice counts any 15 trophy fish; the others need every
 * one of the 18 fish at that tier or better (Master: every tier of every fish).
 */
public enum TrophyFisherRank {
    NOVICE("Novice Trophy Fisher", "<e>", 15),
    ADEPT("Adept Trophy Fisher", "<7>", 18),
    EXPERT("Expert Trophy Fisher", "<6>", 18),
    MASTER("Master Trophy Fisher", "<b>", 18);

    private final String displayName;
    private final String colour;
    private final int goal;

    TrophyFisherRank(String displayName, String colour, int goal) {
        this.displayName = displayName;
        this.colour = colour;
        this.goal = goal;
    }

    public String displayName() {
        return displayName;
    }

    public String coloredName() {
        return colour + displayName;
    }

    public int goal() {
        return goal;
    }

    public int progress(DatapointTrophyFish.TrophyFishData data) {
        if (this == NOVICE) {
            return data.getFish().values().stream().mapToInt(DatapointTrophyFish.FishProgress::getTotalCatches).sum();
        }
        int count = 0;
        for (TrophyFishDefinition fish : fishList()) {
            DatapointTrophyFish.FishProgress progress = data.getFish().get(fish.id());
            if (progress != null && meets(progress)) count++;
        }
        return count;
    }

    private boolean meets(DatapointTrophyFish.FishProgress progress) {
        return switch (this) {
            case NOVICE -> progress.getTotalCatches() > 0;
            case ADEPT -> progress.getSilver() + progress.getGold() + progress.getDiamond() > 0;
            case EXPERT -> progress.getGold() + progress.getDiamond() > 0;
            case MASTER -> progress.getBronze() > 0 && progress.getSilver() > 0 && progress.getGold() > 0 && progress.getDiamond() > 0;
        };
    }

    public boolean achieved(DatapointTrophyFish.TrophyFishData data) {
        return progress(data) >= goal;
    }

    public static @Nullable TrophyFisherRank highest(DatapointTrophyFish.TrophyFishData data) {
        TrophyFisherRank best = null;
        for (TrophyFisherRank rank : values()) {
            if (rank.achieved(data)) best = rank;
        }
        return best;
    }

    public static boolean has(DatapointTrophyFish.TrophyFishData data, TrophyFisherRank rank) {
        return rank.achieved(data);
    }

    public @Nullable TrophyTier tier() {
        return switch (this) {
            case NOVICE -> TrophyTier.BRONZE;
            case ADEPT -> TrophyTier.SILVER;
            case EXPERT -> TrophyTier.GOLD;
            case MASTER -> TrophyTier.DIAMOND;
        };
    }

    private static List<TrophyFishDefinition> fishList() {
        return FishingRegistry.getTrophyFish();
    }
}
