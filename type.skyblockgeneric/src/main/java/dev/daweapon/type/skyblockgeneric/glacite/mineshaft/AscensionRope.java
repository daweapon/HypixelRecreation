package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;

public final class AscensionRope {
    public static final String ID = "ASCENSION_ROPE";

    private AscensionRope() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (!MineshaftService.isInside(player)) {
                player.sendMessage("<c>The Ascension Rope only works inside a Glacite Mineshaft!");
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            MineshaftService.exit(player, true);
        }).build());
    }
}
