package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeContext;

public final class SaltyAbility implements ReforgeAbility.Statistics {
    @Override
    public String reforgeId() {
        return "salty";
    }

    @Override
    public ItemStatistics statistics(ReforgeContext context) {
        return context.world().server() == ServerType.SKYBLOCK_JERRYS_WORKSHOP
                ? context.reforgeStatistics() : ItemStatistics.empty();
    }
}
