package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class EnchantmentTripleStrike extends CatalogueEnchantment implements EventBasedEnchant {
    public static final double[] PERCENT = {10, 20, 30, 40, 50};
    public static final int HITS = 3;
    private static final Map<SkyBlockMob, Integer> HITS_TAKEN = Collections.synchronizedMap(new WeakHashMap<>());

    public EnchantmentTripleStrike() {
        super("TRIPLE_STRIKE");
    }

    public static boolean boosts(int hitNumber) {
        return hitNumber >= 1 && hitNumber <= HITS;
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (!(receiver instanceof SkyBlockMob mob) || level < 1 || level > PERCENT.length) return ItemStatistics.empty();
        int hit = HITS_TAKEN.merge(mob, 1, Integer::sum);
        if (!boosts(hit)) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, PERCENT[level - 1] * CombatPetHooks.firstPounceMultiplier(causer)).build();
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
