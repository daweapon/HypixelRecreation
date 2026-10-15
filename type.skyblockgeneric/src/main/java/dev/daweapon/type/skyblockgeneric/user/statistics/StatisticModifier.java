package dev.daweapon.type.skyblockgeneric.user.statistics;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public record StatisticModifier(
    String name,
    Material material,
    @Nullable String texture,
    ItemStatistics statistics,
    StatisticSourceType sourceType,
    StatisticModifierType modifierType,
    @Nullable String parentName
) {
}
