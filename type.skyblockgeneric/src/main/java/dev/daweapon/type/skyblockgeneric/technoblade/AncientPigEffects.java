package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AncientPigEffects {
    private static final ItemStatistic[] INFUSED_STATISTICS = {
            ItemStatistic.HEALTH, ItemStatistic.DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
            ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED,
            ItemStatistic.ABILITY_DAMAGE, ItemStatistic.TRUE_DEFENSE, ItemStatistic.FEROCITY,
            ItemStatistic.HEALTH_REGENERATION, ItemStatistic.VITALITY, ItemStatistic.MENDING, ItemStatistic.SWING_RANGE,
            ItemStatistic.SPEED, ItemStatistic.MAGIC_FIND, ItemStatistic.PET_LUCK, ItemStatistic.HEAT_RESISTANCE,
            ItemStatistic.COLD_RESISTANCE, ItemStatistic.RESPIRATION, ItemStatistic.PRESSURE_RESISTANCE,
            ItemStatistic.FEAR, ItemStatistic.TRACKING,
    };
    private static final Map<UUID, Long> INFUSED_UNTIL = new ConcurrentHashMap<>();

    private AncientPigEffects() {
    }

    public static boolean active(AncientPigEffect effect) {
        return TechnobladeShrine.activeEffect() == effect;
    }

    public static double skillExperienceMultiplier(SkillCategories category) {
        AncientPigEffect effect = TechnobladeShrine.activeEffect();
        if (effect == null || effect.skill() != category) return 1;
        return 1 + AncientPigEffect.SKILL_XP_BONUS_PERCENT / 100D;
    }

    public static double runicChanceMultiplier() {
        return active(AncientPigEffect.INCREASED_RUNIC_CHANCE) ? AncientPigEffect.RUNIC_CHANCE_FACTOR : 1;
    }

    public static int dropAmount(int amount) {
        return active(AncientPigEffect.DOUBLE_MOB_DROPS) ? amount * 2 : amount;
    }

    public static ItemStatistics petStatistics(ItemStatistics statistics) {
        return active(AncientPigEffect.DOUBLE_PET_STATS) ? ItemStatistics.add(statistics, statistics) : statistics;
    }

    public static void onFairySoulClicked(SkyBlockPlayer player) {
        if (!active(AncientPigEffect.FAIRY_SOUL_INFUSION)) return;
        INFUSED_UNTIL.put(player.getUuid(), System.currentTimeMillis() + AncientPigEffect.INFUSION_MILLIS);
        player.sendMessage("<d>Fairy Soul Infusion! <e>Your stats are buffed by <b>50% <e>for <b>60s<e>.");
    }

    public static ItemStatistics infusion(SkyBlockPlayer player) {
        Long until = INFUSED_UNTIL.get(player.getUuid());
        if (until == null || !active(AncientPigEffect.FAIRY_SOUL_INFUSION)) return ItemStatistics.empty();
        if (System.currentTimeMillis() >= until) {
            INFUSED_UNTIL.remove(player.getUuid());
            return ItemStatistics.empty();
        }
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : INFUSED_STATISTICS) {
            builder.withMultiplicativePercentage(statistic, AncientPigEffect.INFUSION_BONUS_PERCENT);
        }
        return builder.build();
    }
}
