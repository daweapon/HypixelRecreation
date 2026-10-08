package net.swofty.type.skyblockgeneric.fishing.registry;

import java.util.List;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.tag.FishingTag;
import net.swofty.type.skyblockgeneric.fishing.trophy.TrophyFisherRank;
import org.jetbrains.annotations.Nullable;

// Config-driven definition of a sea creature: its required fishing level, XP reward, the availability
// tags (region/medium/etc.) it must satisfy to spawn, and any Trophy Fisher rank it is locked behind.
public record SeaCreatureDefinition(
    String id,
    int requiredFishingLevel,
    double skillXp,
    List<FishingTag> tags,
    @Nullable TrophyFisherRank requiredTrophyRank
) {
    public boolean hasTag(FishingTag tag) {
        return tags.contains(tag);
    }

    public boolean isAvailable(FishingContext context) {
        for (FishingTag tag : tags) {
            if (!tag.isAvailable(context)) {
                return false;
            }
        }
        return requiredTrophyRank == null || requiredTrophyRank.achieved(context.player().getTrophyFishData());
    }
}
