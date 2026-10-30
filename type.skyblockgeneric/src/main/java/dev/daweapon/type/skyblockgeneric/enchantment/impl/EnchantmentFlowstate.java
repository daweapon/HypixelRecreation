package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.PlayerContextEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class EnchantmentFlowstate extends CatalogueEnchantment implements PlayerContextEnchant {
    public static final int MAX_BLOCKS = 200;
    public static final long RESET_MILLIS = 10_000;

    private record Streak(int blocks, long lastBlock) {
    }

    private static final Map<SkyBlockPlayer, Streak> STREAKS = Collections.synchronizedMap(new WeakHashMap<>());

    public EnchantmentFlowstate() {
        super("FLOWSTATE");
    }

    public static void onBlockMined(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        STREAKS.merge(player, new Streak(1, now), (old, ignored) ->
                new Streak(now - old.lastBlock() > RESET_MILLIS ? 1 : Math.min(MAX_BLOCKS, old.blocks() + 1), now));
    }

    public static int blocks(SkyBlockPlayer player, long now) {
        Streak streak = STREAKS.get(player);
        return streak == null || now - streak.lastBlock() > RESET_MILLIS ? 0 : streak.blocks();
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int level) {
        int blocks = blocks(player, System.currentTimeMillis());
        if (blocks == 0 || level < 1) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MINING_SPEED, (double) level * blocks).build();
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
