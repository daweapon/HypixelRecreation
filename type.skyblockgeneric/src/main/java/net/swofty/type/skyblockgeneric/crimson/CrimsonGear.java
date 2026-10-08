package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class CrimsonGear {
    private static final Set<ItemType> RAMPART = Set.of(ItemType.RAMPART_HELMET, ItemType.RAMPART_CHESTPLATE,
            ItemType.RAMPART_LEGGINGS, ItemType.RAMPART_BOOTS);
    private static final ItemStatistics RAMPART_ON_CRIMSON_ISLE = ItemStatistics.builder()
            .withBase(ItemStatistic.HEALTH, 50D)
            .withBase(ItemStatistic.STRENGTH, 20D)
            .withBase(ItemStatistic.CRITICAL_DAMAGE, 15D)
            .build();

    private CrimsonGear() {
    }

    public static ItemStatistics extraItemStatistics(SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type == null) return ItemStatistics.empty();
        ItemStatistics statistics = ItemStatistics.empty();
        if (RAMPART.contains(type) && CrimsonLocation.isCrimsonIsleServer()) {
            statistics = ItemStatistics.add(statistics, RAMPART_ON_CRIMSON_ISLE);
        }
        return statistics;
    }
}
