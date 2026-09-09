package net.swofty.type.skyblockgeneric.enchantment.abstr;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Hook for enchantments whose stat bonus is computed dynamically at the moment damage occurs.
public interface EventBasedEnchant {
    default ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        return ItemStatistics.empty();
    }
}
