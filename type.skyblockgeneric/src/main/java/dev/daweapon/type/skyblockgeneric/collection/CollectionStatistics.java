package dev.daweapon.type.skyblockgeneric.collection;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class CollectionStatistics {
    private record Grant(ItemType type, int requirement, ItemStatistic statistic, double value) {
    }

    private static volatile List<Grant> grants;

    private CollectionStatistics() {
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        boolean any = false;
        for (Grant grant : grants()) {
            if (player.getCollection().get(grant.type()) < grant.requirement()) continue;
            builder.withBase(grant.statistic(), grant.value());
            any = true;
        }
        return any ? builder.build() : ItemStatistics.empty();
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
