package net.swofty.type.skyblockgeneric.garden.chip;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;

public final class ChipItems {
    public static final String HANDLER = "GARDEN_CHIP";

    private ChipItems() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            GardenChip chip = GardenChip.ofItem(item.getAttributeHandler().getTypeAsString());
            if (chip == null) return;
            ItemType type = item.getAttributeHandler().getPotentialType();
            int count = player.isSneaking() ? Math.max(1, item.getAmount()) : 1;
            player.removeItemFromPlayer(type, count);
            ChipService.consume(player, chip, count);
        }).build());
    }
}
