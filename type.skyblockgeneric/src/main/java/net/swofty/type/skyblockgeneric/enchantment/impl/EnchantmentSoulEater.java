package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentSoulEater extends CatalogueEnchantment implements EventBasedEnchant, KillEventEnchant {
    public static final double MAX_BONUS = 1_000_000;
    private static final Map<UUID, Double> PENDING = new ConcurrentHashMap<>();

    public EnchantmentSoulEater() {
        super("SOUL_EATER");
    }

    public static double bonus(int level, double killedMobDamage) {
        return Math.min(MAX_BONUS, Math.max(0, killedMobDamage) * level * 2);
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        PENDING.put(player.getUuid(), bonus(level, killedMob.getStatistics().getOverall(ItemStatistic.DAMAGE)));
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        Double pending = PENDING.remove(causer.getUuid());
        if (pending == null || pending <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, pending).build();
    }

    @Override
    protected @Nullable String limitation() {
        return "Applies to your next hit, not only critical hits";
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
