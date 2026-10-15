package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Hook for enchantments whose stat bonus is computed dynamically at the moment damage occurs.
public interface EventBasedEnchant {
    default ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        return ItemStatistics.empty();
    }
}
