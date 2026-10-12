package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;
import net.swofty.type.skyblockgeneric.reforge.ReforgeContext;

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
