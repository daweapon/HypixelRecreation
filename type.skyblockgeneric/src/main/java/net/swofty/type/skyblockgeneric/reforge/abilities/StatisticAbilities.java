package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

import java.util.List;
import java.util.Set;

public final class StatisticAbilities {
    static final List<ItemStatistic> COMBAT_STATISTICS = List.of(ItemStatistic.HEALTH, ItemStatistic.DEFENSE,
            ItemStatistic.TRUE_DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
            ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED,
            ItemStatistic.ABILITY_DAMAGE, ItemStatistic.FEROCITY);
    static final Set<ServerType> MINING_ISLANDS = Set.of(ServerType.SKYBLOCK_GOLD_MINE,
            ServerType.SKYBLOCK_DEEP_CAVERNS, ServerType.SKYBLOCK_DWARVEN_MINES, ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);

    private StatisticAbilities() {
    }

    public static List<ReforgeAbility> all() {
        return List.of(perfect(), auspicious(), renowned(), fruitful(), trashy(), glacial(), loving(),
                suspicious(), mithraic(), thorny());
    }

    static ReforgeAbility perfect() {
        return ConditionalStatisticsAbility.always("perfect",
                ItemStatistics.builder().withAdditivePercentage(ItemStatistic.DEFENSE, 2D).build());
    }

    static ReforgeAbility auspicious() {
        return ConditionalStatisticsAbility.always("auspicious",
                ItemStatistics.builder().withAdditivePercentage(ItemStatistic.MINING_FORTUNE, 1D).build());
    }

    static ReforgeAbility renowned() {
        ItemStatistics.Builder builder = ItemStatistics.builder().withAdditivePercentage(ItemStatistic.MAGIC_FIND, 1D);
        COMBAT_STATISTICS.forEach(statistic -> builder.withAdditivePercentage(statistic, 1D));
        return ConditionalStatisticsAbility.always("renowned", builder.build());
    }

    static ReforgeAbility fruitful() {
        return new ConditionalStatisticsAbility("fruitful", world -> world.isOn(MINING_ISLANDS),
                world -> ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, 75D).build());
    }

    static ReforgeAbility trashy() {
        return new ConditionalStatisticsAbility("trashy", world -> world.server() == ServerType.SKYBLOCK_BACKWATER_BAYOU,
                world -> ItemStatistics.builder().withBase(ItemStatistic.TREASURE_CHANCE, 1D).build());
    }

    static ReforgeAbility glacial() {
        return ConditionalStatisticsAbility.always("glacial", world -> ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_FORTUNE, 2D * Math.max(0, -world.cold())).build());
    }

    static ReforgeAbility loving() {
        return ConditionalStatisticsAbility.always("loving",
                ItemStatistics.builder().withBase(ItemStatistic.ABILITY_DAMAGE, 5D).build());
    }

    static ReforgeAbility suspicious() {
        return ConditionalStatisticsAbility.always("suspicious",
                ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, 15D).build());
    }

    static ReforgeAbility mithraic() {
        return ConditionalStatisticsAbility.always("mithraic",
                ItemStatistics.builder().withBase(ItemStatistic.DWARVEN_METAL_FORTUNE, 50D).build());
    }

    static ReforgeAbility thorny() {
        return ConditionalStatisticsAbility.always("thorny", world -> ItemStatistics.builder()
                .withBase(ItemStatistic.OVERBLOOM, 0.1D * world.armorThornsTiers()).build());
    }
}
