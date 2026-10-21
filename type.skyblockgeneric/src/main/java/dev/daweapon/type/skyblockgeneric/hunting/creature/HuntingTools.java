package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;

public final class HuntingTools {
    public static final String LASSO = "HUNTING_LASSO";
    public static final String NET = "HUNTING_FISHING_NET";
    public static final String BLACK_HOLE = "HUNTING_BLACK_HOLE";

    private HuntingTools() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(LASSO, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            LassoService.Tier tier = LassoService.tierOf(item.getAttributeHandler().getPotentialType());
            if (tier != null) LassoService.use(player, tier);
        }).build());
        InteractableRegistry.register(NET, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            CreatureSpec.NetTier tier = FishingNetService.tierOf(item.getAttributeHandler().getPotentialType());
            if (tier != null) FishingNetService.use(player, tier);
        }).build());
        InteractableRegistry.register(BLACK_HOLE, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> BlackHoleService.place(player)).build());
    }

    public static boolean isHuntingTool(ItemType type) {
        return LassoService.tierOf(type) != null || FishingNetService.tierOf(type) != null
                || BlackHoleService.HUNTER_FORTUNE.containsKey(type) || Huntaxes.of(type) != null;
    }
}
