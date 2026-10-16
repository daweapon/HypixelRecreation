package dev.daweapon.type.skyblockgeneric.collection;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CollectionStatistics {
    private record Grant(ItemType type, int requirement, ItemStatistic statistic, double value) {
    }

    private static volatile List<Grant> grants;

    private CollectionStatistics() {
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        Map<ItemStatistic, Double> totals = new EnumMap<>(ItemStatistic.class);
        for (Grant grant : grants()) {
            if (player.getCollection().get(grant.type()) < grant.requirement()) continue;
            totals.merge(grant.statistic(), grant.value(), Double::sum);
        }
        if (totals.isEmpty()) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        totals.forEach(builder::withBase);
        return builder.build();
    }

    private static List<Grant> grants() {
        List<Grant> loaded = grants;
        if (loaded != null) return loaded;
        List<Grant> built = new ArrayList<>();
        for (CollectionCategory category : CollectionCategories.getCategories()) {
            for (CollectionCategory.ItemCollection collection : category.getCollections()) {
                for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
                    for (CollectionCategory.Unlock unlock : reward.unlocks()) {
                        if (unlock instanceof CollectionCategory.UnlockStat stat) {
                            built.add(new Grant(collection.type(), reward.requirement(), stat.statistic(), stat.value()));
                        }
                    }
                }
            }
        }
        grants = built;
        return built;
    }
}
