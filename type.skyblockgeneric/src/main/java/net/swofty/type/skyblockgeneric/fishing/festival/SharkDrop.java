package net.swofty.type.skyblockgeneric.fishing.festival;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * One extra row of a Shark's drop table besides its Shark Fins and tooth. Rows whose item this
 * server lacks (the Megalodon Pet, Aquamarine Dye) keep their real name and chance with a null
 * item, so they become live the moment the item exists.
 */
public record SharkDrop(@Nullable ItemType item, String name, int amount, double chancePercent) {
    public boolean isAvailable() {
        return item != null;
    }
}
