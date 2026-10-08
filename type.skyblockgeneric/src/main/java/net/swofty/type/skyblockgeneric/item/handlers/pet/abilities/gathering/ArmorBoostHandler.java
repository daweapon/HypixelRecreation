package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

/**
 * Raises the base stats of the listed armor pieces the player is wearing by {0}%.
 */
public final class ArmorBoostHandler implements PetAbilityHandler {
    private final Set<ItemType> pieces;

    public ArmorBoostHandler(Set<ItemType> pieces) {
        this.pieces = pieces;
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.empty();
        for (SkyBlockItem piece : player.getArmor()) {
            if (!pieces.contains(piece.getAttributeHandler().getPotentialType())) continue;
            total = ItemStatistics.add(total, piece.getAttributeHandler().getBaseStatistics());
        }
        return BaseStatBoostHandler.scaled(total, context.value(0) / 100D);
    }
}
