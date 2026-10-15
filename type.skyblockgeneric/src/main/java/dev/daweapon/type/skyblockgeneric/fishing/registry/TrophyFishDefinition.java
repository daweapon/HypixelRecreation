package dev.daweapon.type.skyblockgeneric.fishing.registry;

import java.util.List;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyCondition;
import org.jetbrains.annotations.Nullable;

/**
 * One of the 18 Trophy Fish. {@code id} is the save key in the player's trophy data; {@code itemStem} is
 * Hypixel's item id stem, so the tiers are {@code <stem>_BRONZE} … {@code <stem>_DIAMOND}.
 */
public record TrophyFishDefinition(
    String id,
    String displayName,
    String itemStem,
    Rarity rarity,
    double catchChance,
    boolean manaScaled,
    boolean specialGoldenFish,
    List<TrophyCondition> conditions,
    List<Integer> filletMagmafish,
    @Nullable String hint
) {
    public boolean conditionsMet(FishingContext context) {
        for (TrophyCondition condition : conditions) {
            if (!condition.test(context)) return false;
        }
        return true;
    }

    public String itemId(TrophyTier tier) {
        return tier.itemId(itemStem);
    }

    public @Nullable ItemType itemType(TrophyTier tier) {
        return ItemType.get(itemId(tier));
    }

    public int filletAmount(TrophyTier tier) {
        return filletMagmafish.size() > tier.ordinal() ? filletMagmafish.get(tier.ordinal()) : 0;
    }

    public String coloredName() {
        String colour = switch (rarity) {
            case UNCOMMON -> "<a>";
            case RARE -> "<9>";
            case EPIC -> "<5>";
            case LEGENDARY -> "<6>";
            case MYTHIC -> "<d>";
            default -> "<f>";
        };
        return colour + displayName;
    }
}
