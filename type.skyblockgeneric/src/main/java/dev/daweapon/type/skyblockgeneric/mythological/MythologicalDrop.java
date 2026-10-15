package dev.daweapon.type.skyblockgeneric.mythological;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * One row of a Mythological creature's drop table. Rows whose item this server does not have yet
 * (such as the Chimera book) keep their real name and chance with a null item, so they show in the
 * catalogue and become live the moment the item exists.
 */
public record MythologicalDrop(@Nullable ItemType item, String name, int amount, double chancePercent) {
    public boolean isAvailable() {
        return item != null;
    }
}
