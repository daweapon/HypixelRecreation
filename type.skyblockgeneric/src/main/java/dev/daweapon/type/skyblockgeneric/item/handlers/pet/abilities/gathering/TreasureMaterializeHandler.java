package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
