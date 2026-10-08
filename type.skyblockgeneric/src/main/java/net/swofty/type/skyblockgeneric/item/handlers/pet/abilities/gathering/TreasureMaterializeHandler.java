package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * A {0}% chance to materialize an item in the inventory whenever a Treasure is caught.
 */
public final class TreasureMaterializeHandler implements PetAbilityHandler {
    private final ItemType item;

    public TreasureMaterializeHandler(ItemType item) {
        this.item = item;
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!TreasureCatches.isTreasure(event)) return;
        SkyBlockPlayer player = event.player();
        if (!LootRoll.roll(player.getUuid(), item, context.value(0))) return;
        SkyBlockItem materialized = new SkyBlockItem(item);
        player.addAndUpdateItem(materialized);
        player.sendMessage("<a>Your pet materialized a " + materialized.getDisplayName() + "<a>!");
    }
}
