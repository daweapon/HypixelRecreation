package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class EnchantmentTitanKiller extends CatalogueEnchantment implements EventBasedEnchant {
    public static final double[] PERCENT_PER_100_DEFENSE = {2, 4, 6, 8, 12, 16, 20};
    public static final double[] PERCENT_CAP = {6, 12, 18, 24, 40, 60, 80};

    public EnchantmentTitanKiller() {
        super("TITAN_KILLER");
    }

    public static double bonus(int level, double targetDefense) {
        double steps = Math.floor(Math.max(0, targetDefense) / 100);
        return Math.min(PERCENT_CAP[level - 1], PERCENT_PER_100_DEFENSE[level - 1] * steps);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (!(receiver instanceof SkyBlockMob mob) || level < 1 || level > PERCENT_CAP.length) return ItemStatistics.empty();
        double bonus = bonus(level, mob.getStatistics().getOverall(ItemStatistic.DEFENSE));
        if (bonus <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, bonus).build();
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
