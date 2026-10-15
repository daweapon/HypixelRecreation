package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentCombo extends CatalogueEnchantment implements EventBasedEnchant, KillEventEnchant {
    private static final Map<UUID, Deque<Long>> KILLS = new ConcurrentHashMap<>();

    public EnchantmentCombo() {
        super("COMBO");
    }

    public static int maxKills(int level) {
        return level * 2;
    }

    public static long windowMillis(int level) {
        return level * 2000L;
    }

    public static double bonus(int level, Iterable<Long> killTimes, long now) {
        int kills = 0;
        for (long time : killTimes) if (now - time <= windowMillis(level)) kills++;
        return level * Math.min(kills, maxKills(level));
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        Deque<Long> kills = KILLS.computeIfAbsent(player.getUuid(), _ -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (kills) {
            kills.addLast(now);
            while (!kills.isEmpty() && now - kills.peekFirst() > windowMillis(level)) kills.pollFirst();
        }
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        Deque<Long> kills = KILLS.get(causer.getUuid());
        if (kills == null) return ItemStatistics.empty();
        double bonus;
        synchronized (kills) {
            bonus = bonus(level, kills, System.currentTimeMillis());
        }
        if (bonus <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, bonus * CombatPetHooks.firstPounceMultiplier(causer)).build();
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
