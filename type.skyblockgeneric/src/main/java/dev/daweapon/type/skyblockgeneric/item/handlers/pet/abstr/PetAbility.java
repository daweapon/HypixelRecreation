package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public interface PetAbility {
    String getName();

    List<String> getDescription(SkyBlockItem instance);

    default ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        return ItemStatistics.empty();
    }

    default void onEvent(PetEvent event, SkyBlockItem pet) {
    }
}