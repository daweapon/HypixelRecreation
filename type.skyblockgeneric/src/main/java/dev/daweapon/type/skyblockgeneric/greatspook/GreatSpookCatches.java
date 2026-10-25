package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.Optional;

public final class GreatSpookCatches {
    public static final String ICY_SINKER_ID = "ICY_SINKER";
    public static final String SPOOKY_BAIT_ID = "SPOOKY_BAIT";
    public static final String SEWER_REGION = "SEWER";
    private static final double ITEM_CATCH_XP = 5;

    private GreatSpookCatches() {
    }

    public static Optional<CatchPayload> resolve(FishingContext context) {
        if (context.medium() != FishingMedium.WATER) return Optional.empty();
        ServerType server = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        if (server == ServerType.SKYBLOCK_HUB && SEWER_REGION.equals(context.regionId())
                && GreatSpookIngredients.questOpen(context.player())) {
            return Optional.of(catchOf(ItemType.SEWER_FISH, "You fished up a Sewer Fish!"));
        }
        if (server == ServerType.SKYBLOCK_SPIDERS_DEN && frozenSpiderGear(context)
                && GreatSpookIngredients.festivalOrQuestOpen(context.player())
                && LootRoll.roll(context.player().getUuid(), ItemType.FROZEN_SPIDER, GreatSpookIngredients.FROZEN_SPIDER_CHANCE)) {
            return Optional.of(catchOf(ItemType.FROZEN_SPIDER, "You fished up a Frozen Spider!"));
        }
        return Optional.empty();
    }

    static boolean frozenSpiderGear(FishingContext context) {
        return context.sinker() != null && ICY_SINKER_ID.equals(context.sinker().getItemId())
                && context.bait() != null && SPOOKY_BAIT_ID.equals(context.bait().getItemId());
    }

    private static CatchPayload catchOf(ItemType type, String message) {
        return new CatchPayload.Quest(type.name(), 1, ITEM_CATCH_XP, message);
    }
}
