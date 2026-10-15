package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

/**
 * Shared, data-driven damage rule for enchantments which target mob classifications.
 */
public interface MobTypeDamageEnchantment extends EventBasedEnchant {
    Set<MobType> affectedMobTypes();

    double[] damageBonuses();

    @Override
    default ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (!(receiver instanceof SkyBlockMob mob) || level < 1 || level > damageBonuses().length) {
            return ItemStatistics.empty();
        }
        if (mob.getMobTypes().stream().noneMatch(affectedMobTypes()::contains)) {
            return ItemStatistics.empty();
        }
        return ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, damageBonuses()[level - 1])
                .build();
    }
}
