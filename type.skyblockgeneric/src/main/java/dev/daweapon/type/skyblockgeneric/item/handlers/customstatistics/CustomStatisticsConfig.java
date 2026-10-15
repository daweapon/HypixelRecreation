package dev.daweapon.type.skyblockgeneric.item.handlers.customstatistics;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.function.Function;

public record CustomStatisticsConfig(Function<SkyBlockItem, ItemStatistics> statisticsProvider) {
}
