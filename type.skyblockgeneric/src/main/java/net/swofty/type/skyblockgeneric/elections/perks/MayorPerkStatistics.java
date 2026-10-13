package net.swofty.type.skyblockgeneric.elections.perks;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor.Perk;

public final class MayorPerkStatistics {
    // The Combat and Miscellaneous groups of the SkyBlock profile menu.
    private static final ItemStatistic[] STATSPOCALYPSE_STATS = {
        ItemStatistic.HEALTH, ItemStatistic.DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
        ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED,
        ItemStatistic.ABILITY_DAMAGE, ItemStatistic.TRUE_DEFENSE, ItemStatistic.FEROCITY,
        ItemStatistic.HEALTH_REGENERATION, ItemStatistic.VITALITY, ItemStatistic.MENDING, ItemStatistic.SWING_RANGE,
        ItemStatistic.SPEED, ItemStatistic.MAGIC_FIND, ItemStatistic.PET_LUCK, ItemStatistic.HEAT_RESISTANCE,
        ItemStatistic.COLD_RESISTANCE, ItemStatistic.RESPIRATION, ItemStatistic.PRESSURE_RESISTANCE,
        ItemStatistic.FEAR, ItemStatistic.TRACKING,
    };

    private MayorPerkStatistics() {}

    public static ItemStatistics flatBonuses(ActivePerks perks, boolean onPublicIsland) {
        ItemStatistics bonuses = ItemStatistics.empty();
        if (onPublicIsland) {
            bonuses = addBase(bonuses, perks, Perk.MINING_XP_BUFF, ItemStatistic.MINING_WISDOM);
            bonuses = addBase(bonuses, perks, Perk.FISHING_XP_BUFF, ItemStatistic.FISHING_WISDOM);
            bonuses = addBase(bonuses, perks, Perk.MINING_FIESTA, ItemStatistic.MINING_WISDOM);
            bonuses = addBase(bonuses, perks, Perk.MINING_FIESTA, ItemStatistic.MINING_FORTUNE);
        }
        bonuses = addBase(bonuses, perks, Perk.MAGIC_XP_BOOST, ItemStatistic.ALCHEMY_WISDOM);
        bonuses = addBase(bonuses, perks, Perk.MAGIC_XP_BOOST, ItemStatistic.ENCHANTING_WISDOM);
        bonuses = addBase(bonuses, perks, Perk.HUNTRESS_INTUITION, ItemStatistic.TRACKING);
        bonuses = addBase(bonuses, perks, Perk.LUCK_OF_THE_SEA, ItemStatistic.SEA_CREATURE_CHANCE);
        return bonuses;
    }

    public static ItemStatistics applyScaling(ActivePerks perks, ItemStatistics total, boolean againstSlayerBoss) {
        if (perks.isActive(Perk.STATSPOCALYPSE)) {
            ItemStatistics.Builder statspocalypse = ItemStatistics.builder();
            for (ItemStatistic statistic : STATSPOCALYPSE_STATS) {
                statspocalypse.withMultiplicativePercentage(statistic, perks.bonus(Perk.STATSPOCALYPSE));
            }
            total = ItemStatistics.add(total, statspocalypse.build());
        }

        if (perks.isActive(Perk.DOUBLE_TROUBLE)) {
            double seaCreatureChance = total.getOverall(ItemStatistic.SEA_CREATURE_CHANCE);
            total = raiseOverall(total, ItemStatistic.DOUBLE_HOOK_CHANCE,
                    seaCreatureChance * perks.bonus(Perk.DOUBLE_TROUBLE));
        }

        if (againstSlayerBoss && perks.isActive(Perk.PATHFINDER)) {
            double magicFind = total.getOverall(ItemStatistic.MAGIC_FIND);
            double pathfinder = perks.bonus(Perk.PATHFINDER);
            total = raiseOverall(total, ItemStatistic.MAGIC_FIND, magicFind * pathfinder / 100D + pathfinder);
        }

        return total;
    }

    private static ItemStatistics raiseOverall(ItemStatistics total, ItemStatistic statistic, double amount) {
        double scaling = total.getAdditive(statistic) * total.getMultiplicative(statistic);
        return scaling <= 0 ? total : total.addBase(statistic, amount / scaling);
    }

    private static ItemStatistics addBase(ItemStatistics bonuses, ActivePerks perks, Perk perk, ItemStatistic statistic) {
        return perks.isActive(perk) ? bonuses.addBase(statistic, perks.bonus(perk)) : bonuses;
    }
}
