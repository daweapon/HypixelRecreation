package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class EnchantmentSwarm extends CatalogueEnchantment implements EventBasedEnchant {
    public static final double RADIUS = 10;
    public static final int MAX_ENEMIES = 10;

    public EnchantmentSwarm() {
        super("SWARM");
    }

    public static double bonus(int level, int enemiesNearby) {
        return level * 2D * Math.min(enemiesNearby, MAX_ENEMIES);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer.getInstance() == null) return ItemStatistics.empty();
        int enemies = 0;
        for (Entity entity : causer.getInstance().getNearbyEntities(causer.getPosition(), RADIUS)) {
            if (entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved()) enemies++;
        }
        double bonus = bonus(level, enemies);
        if (bonus <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, bonus).build();
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
