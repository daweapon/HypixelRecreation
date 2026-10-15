package dev.daweapon.type.skyblockgeneric.fishing.festival;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * One extra row of a Shark's drop table besides its Shark Fins and tooth, with the rarity a pet
 * drops at. Rows whose item this server lacks (Aquamarine Dye) keep their real name and chance
 * with a null item, so they become live the moment the item exists.
 */
public record SharkDrop(@Nullable ItemType item, @Nullable Rarity rarity, String name, int amount, double chancePercent) {
    public SharkDrop(@Nullable ItemType item, String name, int amount, double chancePercent) {
        this(item, null, name, amount, chancePercent);
    }

    public boolean isAvailable() {
        return item != null;
    }
}
